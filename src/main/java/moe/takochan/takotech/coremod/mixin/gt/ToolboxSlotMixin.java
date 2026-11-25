package moe.takochan.takotech.coremod.mixin.gt;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.api.enums.ToolboxSlot;
import moe.takochan.takotech.utils.ToolboxHelper;

@Mixin(value = ToolboxSlot.class, remap = false)
public abstract class ToolboxSlotMixin {

    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void takoTech$preventNestedToolbox(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (ToolboxHelper.hasDetachedToolbox(stack)) cir.setReturnValue(false);
    }
}
