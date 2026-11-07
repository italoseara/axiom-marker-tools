package io.github.italoseara.markertools.screen;

import io.github.italoseara.markertools.Lang;
import com.moulberry.axiom.screen.CreateDisplayEntityScreen;
import io.github.italoseara.markertools.MarkerSettings;
import io.github.italoseara.markertools.MarkerStyle;
import io.github.italoseara.markertools.axiom.MarkerNbt;
import io.github.italoseara.markertools.axiom.MarkerPackets;
import io.github.italoseara.markertools.mixin.CreateDisplayEntityScreenAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Creates a marker. Opened two ways:
 *
 * <ul>
 *   <li>as the fifth tab of Axiom's "create display entity" screen, drawn with the same panel and
 *       tabs so it reads as part of it; the other tabs switch back to Axiom's screen. The box size
 *       is typed in and the marker is centered on the player's feet.
 *   <li>from the marker builder tool, with the box fixed to the tool's selection.
 * </ul>
 */
public final class MarkerCreateScreen extends Screen {

    private static final int WIDTH = 200;
    /** Index of the marker tab, right after Axiom's four display tabs. */
    public static final int TAB_INDEX = 4;

    @Nullable private final CreateDisplayEntityScreen displayScreen;
    @Nullable private final BlockPos selectionMin;
    @Nullable private final BlockPos selectionMax;
    private final StyleForm form = new StyleForm(MarkerSettings.style());
    private final int[] size = MarkerSettings.size().clone();

    private int left;
    private int top;
    private int panelHeight;

    private MarkerCreateScreen(
            @Nullable CreateDisplayEntityScreen displayScreen,
            @Nullable BlockPos selectionMin,
            @Nullable BlockPos selectionMax) {
        super(Component.translatable("markertools.marker"));
        this.displayScreen = displayScreen;
        this.selectionMin = selectionMin;
        this.selectionMax = selectionMax;
    }

    public static MarkerCreateScreen fromDisplayTab(CreateDisplayEntityScreen displayScreen) {
        return new MarkerCreateScreen(displayScreen, null, null);
    }

    public static MarkerCreateScreen forSelection(BlockPos a, BlockPos b) {
        return new MarkerCreateScreen(null, BlockPos.min(a, b), BlockPos.max(a, b));
    }

    private boolean hasSelection() {
        return selectionMin != null && selectionMax != null;
    }

    @Override
    protected void init() {
        panelHeight = 20 + StyleForm.HEIGHT + (hasSelection() ? 16 : 34) + 30;
        left = (width - WIDTH) / 2;
        top = (height - panelHeight) / 2 + (displayScreen != null ? AxiomGui.TAB_RISE / 2 : 0);

        int x = left + 8;
        int innerWidth = WIDTH - 16;
        form.build(this, font, this::addRenderableWidget, x, top + 20, innerWidth, this::rebuildWidgets);

        int y = top + 20 + StyleForm.HEIGHT + 4;
        if (!hasSelection()) {
            int boxWidth = (innerWidth - 8) / 3;
            for (int axis = 0; axis < 3; axis++) {
                int index = axis;
                EditBox box =
                        new EditBox(
                                font, x + axis * (boxWidth + 4), y + 10, boxWidth, 18, Component.empty());
                box.setMaxLength(4);
                box.setFilter(text -> text.chars().allMatch(Character::isDigit));
                box.setValue(Integer.toString(size[index]));
                box.setResponder(
                        text -> size[index] = text.isEmpty() ? 1 : Math.max(1, Integer.parseInt(text)));
                addRenderableWidget(box);
            }
        }

        int buttonY = top + panelHeight - 28;
        int half = (innerWidth - 4) / 2;
        addRenderableWidget(
                Button.builder(Component.translatable("gui.cancel"), b -> onClose())
                        .bounds(x, buttonY, half, 20)
                        .build());
        addRenderableWidget(
                Button.builder(Component.translatable("gui.done"), b -> done())
                        .bounds(x + half + 4, buttonY, innerWidth - half - 4, 20)
                        .build());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(graphics, mouseX, mouseY, delta);
        if (displayScreen != null) {
            ItemStack[] icons = CreateDisplayEntityScreenAccessor.markertools$tabItemStacks();
            for (int i = 0; i < TAB_INDEX; i++) {
                int tabX = left + i * AxiomGui.TAB_STEP;
                AxiomGui.unselectedTab(graphics, i, tabX, top);
                if (i < icons.length) {
                    graphics.renderItem(icons[i], tabX + 5, top - AxiomGui.TAB_RISE + 9);
                }
            }
        }
        AxiomGui.panel(graphics, left, top, WIDTH, panelHeight);
        if (displayScreen != null) {
            int tabX = left + TAB_INDEX * AxiomGui.TAB_STEP;
            AxiomGui.selectedTab(graphics, TAB_INDEX, tabX, top);
            AxiomGui.markerTabIcon(graphics, tabX + 5, top - AxiomGui.TAB_RISE + 9);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        AxiomGui.label(graphics, font, Lang.t("markertools.marker"), left + 8, top + 7);
        form.renderLabels(graphics, font);
        int y = top + 20 + StyleForm.HEIGHT + 4;
        if (hasSelection()) {
            AxiomGui.label(
                    graphics,
                    font,
                    Lang.t(
                            "markertools.selection",
                                    selectionMax.getX() - selectionMin.getX() + 1,
                                    selectionMax.getY() - selectionMin.getY() + 1,
                                    selectionMax.getZ() - selectionMin.getZ() + 1),
                    left + 8,
                    y + 2);
        } else {
            AxiomGui.label(graphics, font, Lang.t("markertools.size_xyz"), left + 8, y);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (displayScreen != null && event.button() == 0) {
            double mx = event.x();
            double my = event.y();
            if (my >= top - AxiomGui.TAB_RISE && my < top) {
                for (int i = 0; i < TAB_INDEX; i++) {
                    int tabX = left + i * AxiomGui.TAB_STEP;
                    if (mx >= tabX && mx < tabX + AxiomGui.TAB_WIDTH) {
                        ((CreateDisplayEntityScreenAccessor) displayScreen).markertools$setScreenType(i);
                        minecraft.setScreen(displayScreen);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void done() {
        MarkerStyle style = form.style();
        MarkerSettings.setStyle(style);
        BlockPos min;
        BlockPos max;
        if (hasSelection()) {
            min = selectionMin;
            max = selectionMax;
        } else {
            System.arraycopy(size, 0, MarkerSettings.size(), 0, 3);
            BlockPos feet = minecraft.player.blockPosition();
            min = feet.offset(-size[0] / 2, 0, -size[2] / 2);
            max = min.offset(size[0] - 1, size[1] - 1, size[2] - 1);
        }
        MarkerPackets.spawn(Vec3.atLowerCornerOf(min), MarkerNbt.spawnTag(style, min, max));
        minecraft.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
