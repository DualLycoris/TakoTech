package moe.takochan.takotech.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import com.cleanroommc.modularui.utils.item.InvWrapper;
import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;
import com.cleanroommc.modularui.utils.item.RangedWrapper;

import gregtech.api.enums.ItemList;
import gregtech.api.enums.ToolboxSlot;
import gregtech.api.items.MetaGeneratedTool;
import gregtech.common.items.ItemGTToolbox;
import gregtech.common.items.toolbox.ToolboxItemStackHandler;
import gregtech.common.items.toolbox.ToolboxUtil;
import moe.takochan.takotech.common.data.ToolData;
import moe.takochan.takotech.common.item.ic2.ItemToolboxPlus;
import moe.takochan.takotech.constants.NBTConstants;

public class ToolboxHelper {

    private static final int LEGACY_SLOT_COUNT = 9;

    private ToolboxHelper() {}

    public static List<ToolData> getToolItems(ItemStack toolbox) {
        List<ToolData> tools = new ArrayList<>();
        if (!isToolboxPlus(toolbox)) return tools;

        if (hasLegacyLayout(toolbox)) {
            NBTTagList contents = getLegacyItems(toolbox.getTagCompound());
            for (int i = 0; i < contents.tagCount(); i++) {
                NBTTagCompound entry = contents.getCompoundTagAt(i);
                int slot = entry.getByte(NBTConstants.TOOLBOX_TOOLS_SLOT);
                ItemStack stack = ItemStack.loadItemStackFromNBT(entry);
                if (isValidSlot(toolbox, slot) && isMetaGeneratedTool(stack)) tools.add(new ToolData(slot, stack));
            }
        } else {
            ToolboxItemStackHandler contents = new ToolboxItemStackHandler(toolbox);
            for (int slot = 0; slot < contents.getSlots(); slot++) {
                ItemStack stack = contents.getStackInSlot(slot);
                if (isMetaGeneratedTool(stack)) tools.add(new ToolData(slot, stack));
            }
        }
        return tools;
    }

    public static boolean isValidSlot(ItemStack toolbox, int slot) {
        return isToolboxPlus(toolbox) && slot >= 0
            && slot < (hasLegacyLayout(toolbox) ? LEGACY_SLOT_COUNT : ToolboxSlot.VALUES.length);
    }

    public static ItemStack getStackInSlot(ItemStack toolbox, int slot) {
        if (!isValidSlot(toolbox, slot)) return null;
        if (!hasLegacyLayout(toolbox)) return new ToolboxItemStackHandler(toolbox).getStackInSlot(slot);

        NBTTagList contents = getLegacyItems(toolbox.getTagCompound());
        int index = findLegacySlot(contents, slot);
        return index < 0 ? null : ItemStack.loadItemStackFromNBT(contents.getCompoundTagAt(index));
    }

    public static void setItemToSlot(ItemStack toolbox, int slot, ItemStack stack) {
        if (!isValidSlot(toolbox, slot)) return;

        if (hasLegacyLayout(toolbox)) {
            NBTTagCompound tag = toolbox.getTagCompound();
            NBTTagList contents = getLegacyItems(tag);
            int index = findLegacySlot(contents, slot);
            if (index >= 0) contents.removeTag(index);
            if (stack != null) {
                NBTTagCompound entry = stack.writeToNBT(new NBTTagCompound());
                entry.setByte(NBTConstants.TOOLBOX_TOOLS_SLOT, (byte) slot);
                contents.appendTag(entry);
            }
            tag.setTag(NBTConstants.TOOLBOX_ITEMS, contents);
        } else {
            if (stack == null && ToolboxUtil.isToolOfType(toolbox, ToolboxSlot.VALUES[slot])) {
                toolbox.getTagCompound()
                    .removeTag(ItemGTToolbox.CURRENT_TOOL_KEY);
            }
            new ToolboxItemStackHandler(toolbox).setStackInSlot(slot, stack);
        }
    }

    public static boolean hasLegacyLayout(ItemStack toolbox) {
        NBTTagCompound tag = toolbox.getTagCompound();
        return tag != null && tag.hasKey(NBTConstants.TOOLBOX_ITEMS, Constants.NBT.TAG_LIST)
            && !tag.hasKey(ItemGTToolbox.CONTENTS_KEY);
    }

    public static void migrateLegacyContents(ItemStack toolbox, EntityPlayer player) {
        NBTTagCompound tag = toolbox.getTagCompound();
        if (tag == null || !tag.hasKey(NBTConstants.TOOLBOX_ITEMS, Constants.NBT.TAG_LIST)) return;

        ToolboxItemStackHandler contents = new ToolboxItemStackHandler(toolbox.copy());
        NBTTagList legacyItems = getLegacyItems(tag);
        NBTTagList pendingItems = new NBTTagList();
        for (int i = 0; i < legacyItems.tagCount(); i++) {
            NBTTagCompound entry = legacyItems.getCompoundTagAt(i);
            ItemStack stack = ItemStack.loadItemStackFromNBT(entry);
            if (stack == null) {
                pendingItems.appendTag(entry);
                continue;
            }

            // Keep dedicated tool and battery slots usable before filling generic storage.
            for (ToolboxSlot type : ToolboxSlot.VALUES) {
                int slot = type.getSlotID();
                if (stack != null && !type.isGeneric() && contents.isItemValid(slot, stack)) {
                    stack = contents.insertItem(slot, stack, false);
                }
            }
            for (ToolboxSlot type : ToolboxSlot.GENERIC_SLOTS) {
                int slot = type.getSlotID();
                if (stack != null && contents.isItemValid(slot, stack)) {
                    stack = contents.insertItem(slot, stack, false);
                }
            }

            if (stack != null && stack.stackSize > 0) {
                pendingItems.appendTag(stack.writeToNBT((NBTTagCompound) entry.copy()));
            }
        }

        ToolboxUtil.saveToolbox(toolbox, contents);
        tag.setTag(NBTConstants.TOOLBOX_ITEMS, pendingItems);
        if (player != null && pendingItems.tagCount() > 0) {
            RangedWrapper inventory = new RangedWrapper(
                new InvWrapper(player.inventory),
                0,
                player.inventory.mainInventory.length);
            for (int i = pendingItems.tagCount() - 1; i >= 0; i--) {
                NBTTagCompound entry = pendingItems.getCompoundTagAt(i);
                ItemStack stack = ItemStack.loadItemStackFromNBT(entry);
                if (stack == null) continue;
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(inventory, stack, false);
                if (remainder == null) {
                    pendingItems.removeTag(i);
                } else {
                    remainder.writeToNBT(entry);
                }
            }
        }
        if (pendingItems.tagCount() == 0) {
            tag.removeTag(NBTConstants.TOOLBOX_ITEMS);
        }
    }

    private static NBTTagList getLegacyItems(NBTTagCompound tag) {
        return tag.getTagList(NBTConstants.TOOLBOX_ITEMS, Constants.NBT.TAG_COMPOUND);
    }

    private static int findLegacySlot(NBTTagList contents, int slot) {
        for (int i = 0; i < contents.tagCount(); i++) {
            if (contents.getCompoundTagAt(i)
                .getByte(NBTConstants.TOOLBOX_TOOLS_SLOT) == slot) return i;
        }
        return -1;
    }

    public static Optional<ItemStack> getToolbox(ItemStack stack) {
        if (isToolboxPlus(stack)) return Optional.of(stack);
        if (!hasDetachedToolbox(stack)) return Optional.empty();

        ItemStack toolbox = ItemStack.loadItemStackFromNBT(
            stack.getTagCompound()
                .getCompoundTag(NBTConstants.TOOLBOX_DATA));
        return isToolboxPlus(toolbox) ? Optional.of(toolbox) : Optional.empty();
    }

    public static boolean isItemToolbox(ItemStack stack) {
        return isToolboxPlus(stack) || hasDetachedToolbox(stack);
    }

    public static boolean hasDetachedToolbox(ItemStack stack) {
        return isMetaGeneratedTool(stack) && stack.hasTagCompound()
            && stack.getTagCompound()
                .hasKey(NBTConstants.TOOLBOX_DATA, Constants.NBT.TAG_COMPOUND);
    }

    public static boolean isToolboxPlus(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemToolboxPlus;
    }

    public static boolean isMetaGeneratedTool(ItemStack stack) {
        return stack != null && stack.getItem() instanceof MetaGeneratedTool;
    }

    public static boolean isUnselectedFieldToolbox(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemGTToolbox
            && ItemList.ToolBox.hasBeenSet()
            && stack.getItem() == ItemList.ToolBox.getItem()
            && !ToolboxUtil.getSelectedToolType(stack)
                .isPresent();
    }
}
