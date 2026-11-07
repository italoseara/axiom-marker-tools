package io.github.italoseara.markertools.screen;

import io.github.italoseara.markertools.Lang;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * HSV color picker with alpha, in Axiom's panel style: saturation/value square, hue bar, alpha bar,
 * old/new preview and an {@code AARRGGBB} hex field. Returns to {@code parent} on close.
 */
public final class ColorPickerScreen extends Screen {

    private static final int WIDTH = 206;
    private static final int HEIGHT = 178;
    private static final int SQUARE = 100;

    private enum Drag {
        NONE,
        SATURATION_VALUE,
        HUE,
        ALPHA
    }

    private final Screen parent;
    private final int originalArgb;
    private final IntConsumer onDone;

    private float hue;
    private float saturation;
    private float value;
    private float alpha;
    private Drag drag = Drag.NONE;
    private EditBox hexBox;
    private boolean updatingHex;

    private int left;
    private int top;

    public ColorPickerScreen(Screen parent, Component title, int argb, IntConsumer onDone) {
        super(title);
        this.parent = parent;
        this.originalArgb = argb;
        this.onDone = onDone;
        setFromArgb(argb);
    }

    @Override
    protected void init() {
        left = (width - WIDTH) / 2;
        top = (height - HEIGHT) / 2;

        hexBox = new EditBox(font, left + 134, top + 72, 64, 18, Component.literal("Hex"));
        hexBox.setMaxLength(9);
        hexBox.setResponder(this::hexEdited);
        addRenderableWidget(hexBox);
        syncHex();

        addRenderableWidget(
                Button.builder(Component.translatable("gui.cancel"), b -> onClose())
                        .bounds(left + 8, top + HEIGHT - 28, 93, 20)
                        .build());
        addRenderableWidget(
                Button.builder(Component.translatable("gui.done"), b -> done())
                        .bounds(left + 105, top + HEIGHT - 28, 93, 20)
                        .build());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(graphics, mouseX, mouseY, delta);
        AxiomGui.panel(graphics, left, top, WIDTH, HEIGHT);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        AxiomGui.label(graphics, font, title.getString(), left + 8, top + 7);

        int sx = left + 8;
        int sy = top + 20;
        for (int i = 0; i < SQUARE; i++) {
            int topColor = 0xFF000000 | Mth.hsvToRgb(hue, i / (float) (SQUARE - 1), 1f);
            graphics.fillGradient(sx + i, sy, sx + i + 1, sy + SQUARE, topColor, 0xFF000000);
        }
        int markX = sx + Math.round(saturation * (SQUARE - 1));
        int markY = sy + Math.round((1f - value) * (SQUARE - 1));
        graphics.renderOutline(markX - 2, markY - 2, 5, 5, 0xFFFFFFFF);
        graphics.renderOutline(sx - 1, sy - 1, SQUARE + 2, SQUARE + 2, 0xFF000000);

        int hx = left + 114;
        for (int i = 0; i < SQUARE; i++) {
            int color = 0xFF000000 | Mth.hsvToRgb(i / (float) SQUARE, 1f, 1f);
            graphics.fill(hx, sy + i, hx + 12, sy + i + 1, color);
        }
        int hueY = sy + Math.round(hue * (SQUARE - 1));
        graphics.fill(hx - 1, hueY - 1, hx + 13, hueY + 1, 0xFFFFFFFF);
        graphics.renderOutline(hx - 1, sy - 1, 14, SQUARE + 2, 0xFF000000);

        int ay = top + 126;
        AxiomGui.checkerboard(graphics, sx, ay, sx + SQUARE, ay + 10);
        int rgb = Mth.hsvToRgb(hue, saturation, value);
        for (int i = 0; i < SQUARE; i++) {
            int a = Math.round(i / (float) (SQUARE - 1) * 255f);
            graphics.fill(sx + i, ay, sx + i + 1, ay + 10, (a << 24) | rgb);
        }
        int alphaX = sx + Math.round(alpha * (SQUARE - 1));
        graphics.fill(alphaX - 1, ay - 1, alphaX + 1, ay + 11, 0xFFFFFFFF);
        graphics.renderOutline(sx - 1, ay - 1, SQUARE + 2, 12, 0xFF000000);

        int px = left + 134;
        AxiomGui.label(graphics, font, Lang.t("markertools.color.new"), px, sy);
        AxiomGui.checkerboard(graphics, px, sy + 10, px + 64, sy + 26);
        graphics.fill(px, sy + 10, px + 64, sy + 26, argb());
        AxiomGui.label(graphics, font, Lang.t("markertools.color.current"), px, sy + 28);
        AxiomGui.checkerboard(graphics, px, sy + 38, px + 64, sy + 50);
        graphics.fill(px, sy + 38, px + 64, sy + 50, originalArgb);
        graphics.renderOutline(px - 1, sy + 9, 66, 18, 0xFF000000);
        graphics.renderOutline(px - 1, sy + 37, 66, 14, 0xFF000000);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            drag = regionAt(event.x(), event.y());
            if (drag != Drag.NONE) {
                applyDrag(event.x(), event.y());
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (drag != Drag.NONE) {
            applyDrag(event.x(), event.y());
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        drag = Drag.NONE;
        return super.mouseReleased(event);
    }

    private Drag regionAt(double mx, double my) {
        int sx = left + 8;
        int sy = top + 20;
        if (mx >= sx && mx < sx + SQUARE && my >= sy && my < sy + SQUARE) {
            return Drag.SATURATION_VALUE;
        }
        if (mx >= left + 114 && mx < left + 126 && my >= sy && my < sy + SQUARE) {
            return Drag.HUE;
        }
        if (mx >= sx && mx < sx + SQUARE && my >= top + 126 && my < top + 136) {
            return Drag.ALPHA;
        }
        return Drag.NONE;
    }

    private void applyDrag(double mx, double my) {
        int sx = left + 8;
        int sy = top + 20;
        float fx = Mth.clamp((float) (mx - sx) / (SQUARE - 1), 0f, 1f);
        float fy = Mth.clamp((float) (my - sy) / (SQUARE - 1), 0f, 1f);
        switch (drag) {
            case SATURATION_VALUE -> {
                saturation = fx;
                value = 1f - fy;
            }
            case HUE -> hue = Math.min(fy, 0.999f);
            case ALPHA -> alpha = fx;
            case NONE -> {}
        }
        syncHex();
    }

    private void hexEdited(String text) {
        if (updatingHex) {
            return;
        }
        String hex = text.startsWith("#") ? text.substring(1) : text;
        try {
            if (hex.length() == 8) {
                setFromArgb((int) Long.parseLong(hex, 16));
            } else if (hex.length() == 6) {
                setFromArgb(Math.round(alpha * 255f) << 24 | Integer.parseInt(hex, 16));
            }
        } catch (NumberFormatException ignored) {
            // Keep the last valid color while the user is still typing.
        }
    }

    private void syncHex() {
        if (hexBox == null) {
            return;
        }
        updatingHex = true;
        hexBox.setValue("%08X".formatted(argb()));
        updatingHex = false;
    }

    private int argb() {
        return Math.round(alpha * 255f) << 24 | Mth.hsvToRgb(hue, saturation, value);
    }

    private void setFromArgb(int argb) {
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        alpha = ((argb >>> 24) & 0xFF) / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        value = max;
        saturation = max == 0 ? 0 : delta / max;
        if (delta == 0) {
            return;
        }
        float h;
        if (max == r) {
            h = ((g - b) / delta) % 6f;
        } else if (max == g) {
            h = (b - r) / delta + 2f;
        } else {
            h = (r - g) / delta + 4f;
        }
        h /= 6f;
        hue = h < 0 ? h + 1f : h;
    }

    private void done() {
        onDone.accept(argb());
        minecraft.setScreen(parent);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
