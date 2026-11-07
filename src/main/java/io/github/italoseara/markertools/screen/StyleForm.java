package io.github.italoseara.markertools.screen;

import io.github.italoseara.markertools.Lang;
import io.github.italoseara.markertools.MarkerPreset;
import io.github.italoseara.markertools.MarkerStyle;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The name, preset, colors and thickness block shared by the create and edit screens. Holds the
 * value outside the widgets so it survives the screen re-running {@code init()} (returning from the
 * color picker, resizing the window).
 */
public final class StyleForm {

    public static final int HEIGHT = 112;
    private static final int MAX_THICKNESS = 10;

    private MarkerStyle style;
    private int labelX;
    private int labelY;
    private int width;

    public StyleForm(MarkerStyle initial) {
        this.style = initial;
    }

    public MarkerStyle style() {
        return style;
    }

    /**
     * Adds the widgets into a {@code width}-wide column at ({@code x}, {@code y}).
     *
     * @param rebuild re-runs the owning screen's {@code init()}, used when a preset swaps every
     *     value at once
     */
    public void build(
            Screen screen,
            Font font,
            Consumer<AbstractWidget> add,
            int x,
            int y,
            int width,
            Runnable rebuild) {
        this.labelX = x;
        this.labelY = y;
        this.width = width;

        EditBox name = new EditBox(font, x, y + 10, width, 18, Component.translatable("markertools.name"));
        name.setMaxLength(64);
        name.setValue(style.name());
        name.setResponder(value -> style = style.withName(value));
        add.accept(name);

        add.accept(
                CycleButton.builder(
                                (MarkerPreset preset) -> Component.translatable(preset.labelKey()),
                                style.preset())
                        .withValues(MarkerPreset.BUILT_IN)
                        .create(
                                x,
                                y + 32,
                                width,
                                20,
                                Component.translatable("markertools.preset"),
                                (button, preset) -> {
                                    style = style.withPreset(preset);
                                    rebuild.run();
                                }));

        int half = (width - 4) / 2;
        add.accept(
                new ColorSwatchButton(
                        x,
                        y + 66,
                        half,
                        20,
                        Component.translatable("markertools.line_color"),
                        () -> style.lineArgb(),
                        () ->
                                Minecraft.getInstance()
                                        .setScreen(
                                                new ColorPickerScreen(
                                                        screen,
                                                        Component.translatable("markertools.line_color"),
                                                        style.lineArgb(),
                                                        argb -> style = style.withLine(argb)))));
        add.accept(
                new ColorSwatchButton(
                        x + half + 4,
                        y + 66,
                        width - half - 4,
                        20,
                        Component.translatable("markertools.face_color"),
                        () -> style.faceArgb(),
                        () ->
                                Minecraft.getInstance()
                                        .setScreen(
                                                new ColorPickerScreen(
                                                        screen,
                                                        Component.translatable("markertools.face_color"),
                                                        style.faceArgb(),
                                                        argb -> style = style.withFace(argb)))));

        add.accept(new ThicknessSlider(x, y + 90, width));
    }

    public void renderLabels(GuiGraphics graphics, Font font) {
        AxiomGui.label(graphics, font, Lang.t("markertools.name"), labelX, labelY);
        AxiomGui.label(graphics, font, Lang.t("markertools.line"), labelX, labelY + 56);
        AxiomGui.label(graphics, font, Lang.t("markertools.face"), labelX + (width - 4) / 2 + 4, labelY + 56);
    }

    private final class ThicknessSlider extends AbstractSliderButton {

        ThicknessSlider(int x, int y, int width) {
            super(
                    x,
                    y,
                    width,
                    20,
                    Component.empty(),
                    (style.lineThickness() - 1) / (double) (MAX_THICKNESS - 1));
            updateMessage();
        }

        private int thickness() {
            return 1 + (int) Math.round(value * (MAX_THICKNESS - 1));
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("markertools.thickness_value", thickness()));
        }

        @Override
        protected void applyValue() {
            style = style.withThickness(thickness());
        }
    }
}
