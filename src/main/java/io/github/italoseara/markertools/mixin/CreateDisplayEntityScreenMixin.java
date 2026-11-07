package io.github.italoseara.markertools.mixin;

import com.moulberry.axiom.screen.CreateDisplayEntityScreen;
import io.github.italoseara.markertools.screen.AxiomGui;
import io.github.italoseara.markertools.screen.MarkerCreateScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds a fifth "Marker" tab after Axiom's item/block/text/sprite display tabs. */
@Mixin(CreateDisplayEntityScreen.class)
public abstract class CreateDisplayEntityScreenMixin {

    /** Axiom's screen types 0..3 are the four tabs; 4..7 are item/block/sprite pickers. */
    private boolean markertools$showsTabs() {
        CreateDisplayEntityScreenAccessor self = (CreateDisplayEntityScreenAccessor) this;
        return self.markertools$canSwitchType() && self.markertools$screenType() <= 3;
    }

    private int markertools$tabX() {
        return ((CreateDisplayEntityScreenAccessor) this).markertools$leftPos()
                + MarkerCreateScreen.TAB_INDEX * AxiomGui.TAB_STEP;
    }

    @Inject(method = "renderBackground", at = @At("TAIL"))
    private void markertools$drawMarkerTab(
            GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!markertools$showsTabs()) {
            return;
        }
        int top = ((CreateDisplayEntityScreenAccessor) this).markertools$topPos();
        AxiomGui.unselectedTab(graphics, MarkerCreateScreen.TAB_INDEX, markertools$tabX(), top);
        AxiomGui.markerTabIcon(graphics, markertools$tabX() + 5, top - AxiomGui.TAB_RISE + 9);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void markertools$clickMarkerTab(
            MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (event.button() != 0 || !markertools$showsTabs()) {
            return;
        }
        int top = ((CreateDisplayEntityScreenAccessor) this).markertools$topPos();
        int tabX = markertools$tabX();
        if (event.x() >= tabX
                && event.x() < tabX + AxiomGui.TAB_WIDTH
                && event.y() >= top - AxiomGui.TAB_RISE
                && event.y() < top) {
            Minecraft.getInstance()
                    .setScreen(MarkerCreateScreen.fromDisplayTab((CreateDisplayEntityScreen) (Object) this));
            cir.setReturnValue(true);
        }
    }
}
