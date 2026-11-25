package moe.takochan.takotech.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import com.gtnewhorizon.gtnhlib.util.ServerThreadUtil;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import gregtech.common.items.ItemGTToolbox;
import io.netty.buffer.ByteBuf;
import moe.takochan.takotech.constants.NBTConstants;
import moe.takochan.takotech.utils.CommonUtils;
import moe.takochan.takotech.utils.ToolboxHelper;

public class PacketToolboxSelected implements IMessage {

    private int slot;

    public PacketToolboxSelected() {}

    public PacketToolboxSelected(int slot) {
        this.slot = slot;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.slot = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.slot);
    }

    public static class Handler implements IMessageHandler<PacketToolboxSelected, IMessage> {

        @Override
        public IMessage onMessage(PacketToolboxSelected message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            ServerThreadUtil.addScheduledTask(() -> selectTool(player, message.slot));
            return null;
        }

        static void selectTool(EntityPlayer player, int slot) {
            ItemStack held = player.inventory.getCurrentItem();
            ItemStack toolbox = ToolboxHelper.getToolbox(held)
                .map(ItemStack::copy)
                .orElse(null);
            if (toolbox == null || (toolbox.hasTagCompound() && toolbox.getTagCompound()
                .getBoolean(ItemGTToolbox.TOOLBOX_OPEN_KEY))) return;

            // Prepare the swap on copies so a stale selection cannot detach the toolbox from the held tool.
            if (ToolboxHelper.hasDetachedToolbox(held)) {
                NBTTagCompound heldTag = held.getTagCompound();
                if (!heldTag.hasKey(NBTConstants.TOOLBOX_SLOT, Constants.NBT.TAG_INT)) return;
                int previousSlot = heldTag.getInteger(NBTConstants.TOOLBOX_SLOT);
                if (!ToolboxHelper.isValidSlot(toolbox, previousSlot)
                    || ToolboxHelper.getStackInSlot(toolbox, previousSlot) != null) return;

                ItemStack previousTool = held.copy();
                NBTTagCompound previousTag = previousTool.getTagCompound();
                previousTag.removeTag(NBTConstants.TOOLBOX_DATA);
                previousTag.removeTag(NBTConstants.TOOLBOX_SLOT);
                ToolboxHelper.setItemToSlot(toolbox, previousSlot, previousTool);
            } else if (slot == -1) {
                return;
            }

            ItemStack replacement = toolbox;
            if (slot != -1) {
                ItemStack tool = ToolboxHelper.getStackInSlot(toolbox, slot);
                if (!ToolboxHelper.isMetaGeneratedTool(tool) || ToolboxHelper.hasDetachedToolbox(tool)) return;

                ToolboxHelper.setItemToSlot(toolbox, slot, null);
                NBTTagCompound toolTag = CommonUtils.openNbtData(tool);
                toolTag.setTag(NBTConstants.TOOLBOX_DATA, toolbox.writeToNBT(new NBTTagCompound()));
                toolTag.setInteger(NBTConstants.TOOLBOX_SLOT, slot);
                replacement = tool;
            }

            player.inventory.setInventorySlotContents(player.inventory.currentItem, replacement);
        }
    }
}
