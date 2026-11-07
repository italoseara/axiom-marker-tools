package io.github.italoseara.markertools.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Drawing helpers that reproduce the look of Axiom's vanilla-style screens. */
public final class AxiomGui {

    /** Axiom's panel sprite, the light gray frame behind every Axiom screen. */
    public static final Identifier PANEL = Identifier.fromNamespaceAndPath("axiom", "background");

    public static final Identifier MARKER_ICON =
            Identifier.fromNamespaceAndPath("axiom-marker-tools", "textures/gui/marker_tool.png");

    /** The structure block: vanilla's own tool for marking out a box region. */
    private static final ItemStack MARKER_TAB_ITEM = new ItemStack(Items.STRUCTURE_BLOCK);

    public static final int LABEL_COLOR = 0xFF404040;
    public static final int TAB_WIDTH = 26;
    public static final int TAB_HEIGHT = 32;
    public static final int TAB_STEP = 27;
    /** How far a top tab rises above the panel. */
    public static final int TAB_RISE = 28;

    private AxiomGui() {}

    public static void panel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PANEL, x, y, width, height);
    }

    public static void label(GuiGraphics graphics, Font font, String text, int x, int y) {
        graphics.drawString(font, text, x, y, LABEL_COLOR, false);
    }

    /** Icon of the marker tab: an item, like Axiom's display tabs, so it is lit and shaded alike. */
    public static void markerTabIcon(GuiGraphics graphics, int x, int y) {
        graphics.renderItem(MARKER_TAB_ITEM, x, y);
    }

    public static void markerIcon(GuiGraphics graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, MARKER_ICON, x, y, 0f, 0f, 16, 16, 16, 16);
    }

    /**
     * An unselected top tab, cut at the panel edge: drawn after the panel, the full sprite would
     * cover the panel's top border, so only the part above it is drawn.
     */
    public static void unselectedTab(GuiGraphics graphics, int index, int x, int panelTop) {
        Identifier sprite =
                Identifier.withDefaultNamespace(
                        "container/creative_inventory/tab_top_unselected_" + (index + 1));
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                sprite,
                TAB_WIDTH,
                TAB_HEIGHT,
                0,
                0,
                x,
                panelTop - TAB_RISE,
                TAB_WIDTH,
                TAB_RISE);
    }

    /** A selected top tab, drawn over the panel so it merges with it. */
    public static void selectedTab(GuiGraphics graphics, int index, int x, int panelTop) {
        Identifier sprite =
                Identifier.withDefaultNamespace(
                        "container/creative_inventory/tab_top_selected_" + (index + 1));
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                sprite,
                x,
                panelTop - TAB_RISE,
                TAB_WIDTH,
                TAB_HEIGHT);
    }

    /** Light/dark checkerboard behind a translucent color swatch. */
    public static void checkerboard(GuiGraphics graphics, int x0, int y0, int x1, int y1) {
        int cell = 4;
        for (int y = y0; y < y1; y += cell) {
            for (int x = x0; x < x1; x += cell) {
                boolean light = (((x - x0) / cell) + ((y - y0) / cell)) % 2 == 0;
                graphics.fill(
                        x,
                        y,
                        Math.min(x + cell, x1),
                        Math.min(y + cell, y1),
                        light ? 0xFFFFFFFF : 0xFFBFBFBF);
            }
        }
    }
}
