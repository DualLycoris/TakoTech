package moe.takochan.takotech.common.recipe;

import java.util.Collections;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.world.World;

import moe.takochan.takotech.utils.RecipeUtils;
import moe.takochan.takotech.utils.ToolboxHelper;

/**
 * 高级工具箱配方，支持从普通工具箱升级时保留NBT数据
 */
public class ToolboxPlusRecipe extends ShapelessRecipes {

    public ToolboxPlusRecipe(ItemStack output, ItemStack input) {
        super(output, Collections.singletonList(input));
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        if (!super.matches(inv, world)) return false;
        for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
            ItemStack input = inv.getStackInSlot(slot);
            if (input != null) return ToolboxHelper.isUnselectedFieldToolbox(input);
        }
        return false;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return RecipeUtils.copyNBTFromInput(inv, this.getRecipeOutput());
    }
}
