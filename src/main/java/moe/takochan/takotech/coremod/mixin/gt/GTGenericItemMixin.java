package moe.takochan.takotech.coremod.mixin.gt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import gregtech.api.items.GTGenericItem;
import moe.takochan.takotech.constants.NameConstants;

@Mixin(value = GTGenericItem.class, remap = false)
public abstract class GTGenericItemMixin {

    // Keep the registry name already stored in worlds while GregTech supplies the item implementation.
    @ModifyArg(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lcpw/mods/fml/common/registry/GameRegistry;registerItem(Lnet/minecraft/item/Item;Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/item/Item;"),
        index = 1,
        require = 1)
    private static String takoTech$keepAdvancedToolboxId(String name) {
        return ("gt.takotech." + NameConstants.ITEM_TOOLBOX_PLUS).equals(name) ? NameConstants.ITEM_TOOLBOX_PLUS : name;
    }
}
