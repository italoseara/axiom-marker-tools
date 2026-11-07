package io.github.italoseara.markertools.mixin;

import com.moulberry.axiom.screen.EditMarkerScreen;
import io.github.italoseara.markertools.screen.MarkerEditScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Swaps Axiom's raw NBT marker editor for {@link MarkerEditScreen}. */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void markertools$replaceMarkerEditor(Screen screen, CallbackInfo ci) {
        if (!(screen instanceof EditMarkerScreen axiomEditor)) {
            return;
        }
        if (MarkerEditScreen.openAxiomEditorOnce) {
            MarkerEditScreen.openAxiomEditorOnce = false;
            return;
        }
        EditMarkerScreenAccessor accessor = (EditMarkerScreenAccessor) axiomEditor;
        ci.cancel();
        ((Minecraft) (Object) this)
                .setScreen(new MarkerEditScreen(accessor.markertools$uuid(), accessor.markertools$data()));
    }
}
