package io.github.italoseara.markertools.mixin;

import com.moulberry.axiom.Axiom;
import com.moulberry.axiom.screen.SwitchHotbarScreen;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds a "marker range" slider to the options page of Axiom's hotbar screen, above "Show Marker
 * Entity Gizmos". It edits Axiom's own {@code markerEntityRange} setting, which Axiom saves.
 */
@Mixin(SwitchHotbarScreen.class)
public abstract class SwitchHotbarScreenMixin extends Screen {

    private static final int MIN_RANGE = 16;
    private static final int MAX_RANGE = 1024;
    private static final int STEP = 16;
    /** Axiom places its options from height - 220 (topmost) down in 22px rows. */
    private static final int ROW_FROM_BOTTOM = 242;

    protected SwitchHotbarScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void markertools$addMarkerRangeSlider(CallbackInfo ci) {
        SwitchHotbarScreenAccessor self = (SwitchHotbarScreenAccessor) this;
        RangeSlider slider = new RangeSlider(width / 2 - 91, height - ROW_FROM_BOTTOM);
        slider.visible = self.markertools$optionsOpen();
        addRenderableWidget(slider);
        self.markertools$optionsWidgets().add(slider);
    }

    private static final class RangeSlider extends AbstractSliderButton {

        RangeSlider(int x, int y) {
            super(x, y, 182, 20, Component.empty(), toValue(Axiom.configuration.entityManipulation.markerEntityRange));
            updateMessage();
        }

        private static double toValue(int range) {
            return (Math.clamp(range, MIN_RANGE, MAX_RANGE) - MIN_RANGE) / (double) (MAX_RANGE - MIN_RANGE);
        }

        private int range() {
            int raw = MIN_RANGE + (int) Math.round(value * (MAX_RANGE - MIN_RANGE));
            return Math.round(raw / (float) STEP) * STEP;
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("markertools.option.range", range()));
        }

        @Override
        protected void applyValue() {
            Axiom.configuration.entityManipulation.markerEntityRange = range();
        }
    }
}
