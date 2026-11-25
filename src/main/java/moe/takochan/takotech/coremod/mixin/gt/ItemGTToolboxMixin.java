package moe.takochan.takotech.coremod.mixin.gt;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.common.items.ItemGTToolbox;
import moe.takochan.takotech.utils.ToolboxHelper;

@Mixin(value = ItemGTToolbox.class, remap = false)
public abstract class ItemGTToolboxMixin {

    // An idle Field Engineer's Toolbox matches the upgrade and is consumed. A selected tool is reusable.
    @Inject(method = "hasContainerItem", at = @At("HEAD"), cancellable = true)
    private void takoTech$consumeIdleToolbox(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (ToolboxHelper.isUnselectedFieldToolbox(stack)) cir.setReturnValue(false);
    }

    @Inject(method = "getContainerItem", at = @At("HEAD"), cancellable = true)
    private void takoTech$skipIdleToolboxRemainder(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (ToolboxHelper.isUnselectedFieldToolbox(stack)) cir.setReturnValue(null);
    }
}
