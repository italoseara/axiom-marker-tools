package io.github.italoseara.markertools.screen;

import java.util.function.IntSupplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.AbstractButton;

/** A vanilla button whose face is the current color over a checkerboard; clicking opens a picker. */
public final class ColorSwatchButton extends AbstractButton {

    private final IntSupplier color;
    private final Runnable onPress;

    public ColorSwatchButton(
            int x, int y, int width, int height, Component label, IntSupplier color, Runnable onPress) {
        super(x, y, width, height, label);
        this.color = color;
        this.onPress = onPress;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        onPress.run();
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderDefaultSprite(graphics);
        int x0 = getX() + 3;
        int y0 = getY() + 3;
        int x1 = getX() + getWidth() - 3;
        int y1 = getY() + getHeight() - 3;
        AxiomGui.checkerboard(graphics, x0, y0, x1, y1);
        graphics.fill(x0, y0, x1, y1, color.getAsInt());
        graphics.renderOutline(x0 - 1, y0 - 1, x1 - x0 + 2, y1 - y0 + 2, 0xFF000000);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
