package io.github.italoseara.markertools.screen;

import io.github.italoseara.markertools.Lang;
import com.moulberry.axiom.marker.MarkerData;
import com.moulberry.axiom.screen.EditMarkerScreen;
import io.github.italoseara.markertools.MarkerPreset;
import io.github.italoseara.markertools.MarkerStore;
import io.github.italoseara.markertools.MarkerStyle;
import io.github.italoseara.markertools.axiom.MarkerNbt;
import io.github.italoseara.markertools.axiom.MarkerPackets;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

/**
 * Replaces Axiom's marker NBT editor (opened by right-clicking a marker) with a form: name, preset,
 * color pickers, thickness and the box corners as plain numbers relative to the marker. "NBT
 * avançado" still opens Axiom's raw editor.
 */
public final class MarkerEditScreen extends Screen {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 20 + StyleForm.HEIGHT + 38;

    /** Lets the next {@link EditMarkerScreen} through instead of swapping it for this screen. */
    public static boolean openAxiomEditorOnce;

    private final UUID markerUuid;
    private final CompoundTag originalData;
    private final StyleForm form;
    private final double[] min = new double[3];
    private final double[] max = new double[3];

    private int left;
    private int top;

    public MarkerEditScreen(UUID markerUuid, CompoundTag data) {
        super(Component.translatable("markertools.marker"));
        this.markerUuid = markerUuid;
        this.originalData = data.copy();

        MarkerPreset preset = MarkerPreset.byType(data.getStringOr("type", null));
        this.form =
                new StyleForm(
                        new MarkerStyle(
                                data.getStringOr("name", ""),
                                preset,
                                data.getIntOr("line_argb", preset.lineArgb()),
                                data.getIntOr("face_argb", preset.faceArgb()),
                                data.getIntOr("line_thickness", preset.lineThickness())));

        MarkerData known = MarkerStore.get(markerUuid);
        Vec3 position = known != null ? known.position() : Vec3.ZERO;
        copy(MarkerNbt.readCornerOffset(data, "min", position), min);
        copy(MarkerNbt.readCornerOffset(data, "max", position), max);
    }

    private static void copy(Vec3 offset, double[] into) {
        if (offset != null) {
            into[0] = offset.x;
            into[1] = offset.y;
            into[2] = offset.z;
        }
    }

    @Override
    protected void init() {
        left = (width - WIDTH) / 2;
        top = (height - HEIGHT) / 2;

        int formWidth = 130;
        form.build(
                this, font, this::addRenderableWidget, left + 8, top + 20, formWidth, this::rebuildWidgets);

        int boxX = left + 8 + formWidth + 10;
        int boxWidth = (WIDTH - 8 - (boxX - left) - 8) / 3;
        addCornerRow(min, boxX, top + 30, boxWidth);
        addCornerRow(max, boxX, top + 66, boxWidth);

        int buttonY = top + HEIGHT - 28;
        int third = (WIDTH - 16 - 8) / 3;
        addRenderableWidget(
                Button.builder(Component.translatable("markertools.advanced_nbt"), b -> openAxiomEditor())
                        .bounds(left + 8, buttonY, third, 20)
                        .build());
        addRenderableWidget(
                Button.builder(Component.translatable("gui.cancel"), b -> onClose())
                        .bounds(left + 8 + third + 4, buttonY, third, 20)
                        .build());
        addRenderableWidget(
                Button.builder(Component.translatable("gui.done"), b -> done())
                        .bounds(left + 8 + (third + 4) * 2, buttonY, third, 20)
                        .build());
    }

    private void addCornerRow(double[] corner, int x, int y, int boxWidth) {
        for (int axis = 0; axis < 3; axis++) {
            int index = axis;
            EditBox box =
                    new EditBox(font, x + axis * (boxWidth + 2), y, boxWidth - 2, 18, Component.empty());
            box.setMaxLength(12);
            box.setFilter(text -> text.matches("-?\\d*\\.?\\d*"));
            box.setValue(MarkerNbt.formatNumber(corner[index]));
            box.setResponder(
                    text -> {
                        try {
                            corner[index] = Double.parseDouble(text);
                        } catch (NumberFormatException ignored) {
                            // "-" or "" while typing; keep the last number.
                        }
                    });
            addRenderableWidget(box);
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(graphics, mouseX, mouseY, delta);
        AxiomGui.panel(graphics, left, top, WIDTH, HEIGHT);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        AxiomGui.label(graphics, font, Lang.t("markertools.marker"), left + 8, top + 7);
        form.renderLabels(graphics, font);

        int boxX = left + 8 + 130 + 10;
        AxiomGui.label(graphics, font, Lang.t("markertools.edit.box"), boxX, top + 7);
        AxiomGui.label(graphics, font, Lang.t("markertools.edit.min_corner"), boxX, top + 20);
        AxiomGui.label(graphics, font, Lang.t("markertools.edit.max_corner"), boxX, top + 56);
        AxiomGui.label(
                graphics,
                font,
                Lang.t(
                        "markertools.size",
                        MarkerNbt.formatNumber(Math.abs(max[0] - min[0])),
                        MarkerNbt.formatNumber(Math.abs(max[1] - min[1])),
                        MarkerNbt.formatNumber(Math.abs(max[2] - min[2]))),
                boxX,
                top + 92);
    }

    private void done() {
        CompoundTag data = MarkerNbt.styleData(form.style());
        data.put(
                "min",
                MarkerNbt.relative(
                        Math.min(min[0], max[0]), Math.min(min[1], max[1]), Math.min(min[2], max[2])));
        data.put(
                "max",
                MarkerNbt.relative(
                        Math.max(min[0], max[0]), Math.max(min[1], max[1]), Math.max(min[2], max[2])));
        MarkerPackets.mergeData(markerUuid, data);
        onClose();
    }

    private void openAxiomEditor() {
        openAxiomEditorOnce = true;
        minecraft.setScreen(new EditMarkerScreen(markerUuid, originalData));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
