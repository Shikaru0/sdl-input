package name.modid.client.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import name.modid.client.input.SdlInputHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.swing.text.JTextComponent;

@Mixin(JTextComponent.KeyBinding.class)
public class KeyBindingMixin {
    @Shadow @Final
    private InputConstants.Key boundKey;

    @Shadow
    private int timesPressed;

    @Inject(at = @At("HEAD"), method = "isPressed", cancellable = true)
    private void onIsPressed(CallbackInfoReturnable<Boolean> cir) {
        SdlInputHandler handler = SdlInputHandler.getInstance();
        if (handler != null && handler.isActive()) {
            cir.setReturnValue(handler.isKeyDown(this.boundKey));
        }
    }

    @Inject(at = @At("HEAD"), method = "wasPressed", cancellable = true)
    private void onWasPressed(CallbackInfoReturnable<Boolean> cir) {
        SdlInputHandler handler = SdlInputHandler.getInstance();
        if (handler != null && handler.isActive()) {
            this.timesPressed = 0;
            cir.setReturnValue(handler.consumePress(this.boundKey));
        }
    }
}