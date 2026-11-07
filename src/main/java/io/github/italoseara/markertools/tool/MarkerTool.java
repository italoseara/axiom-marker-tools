package io.github.italoseara.markertools.tool;

import io.github.italoseara.markertools.Lang;
import com.moulberry.axiom.RayCaster;
import com.moulberry.axiom.UserAction;
import com.moulberry.axiom.editor.EditorUI;
import com.moulberry.axiom.gizmo.Gizmo;
import com.moulberry.axiom.render.AxiomWorldRenderContext;
import com.moulberry.axiom.render.renderable.BoundingBoxStyle;
import com.moulberry.axiom.render.renderable.RenderableBoundingBox;
import com.moulberry.axiom.restrictions.AxiomPermission;
import com.moulberry.axiom.tools.Tool;
import io.github.italoseara.markertools.MarkerSettings;
import io.github.italoseara.markertools.MarkerStyle;
import io.github.italoseara.markertools.axiom.MarkerNbt;
import io.github.italoseara.markertools.axiom.MarkerPackets;
import imgui.moulberry92.ImGui;
import imgui.moulberry92.type.ImString;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

/**
 * Axiom toolbar tool that creates a marker region. Click a block for the first corner and another
 * for the second; both corners then get Axiom's own gizmos, the same ones the Box Select tool uses
 * to resize a selection. Enter (or the panel button) spawns the marker through Axiom's spawn
 * packet, so AxiomPaper's {@code axiom.entity.spawn} permission governs it.
 */
public final class MarkerTool implements Tool {

    /** The generic custom-tool glyph from Axiom's icon font, until we ship our own. */
    private static final char ICON = (char) 59666;

    private final RenderableBoundingBox preview = new RenderableBoundingBox();
    private final ImString name = new ImString(64);
    private final float[] lineColor = {1f, 0.33f, 0.33f, 1f};
    private final float[] faceColor = {1f, 0.33f, 0.33f, 0.25f};
    private final int[] lineThickness = {2};

    private Gizmo firstCorner;
    private Gizmo secondCorner;

    @Override
    public UserAction.ActionResult callAction(UserAction action, Object data) {
        return switch (action) {
            case LEFT_MOUSE -> leftClick();
            case RIGHT_MOUSE, ESCAPE -> {
                if (!hasSelection()) {
                    yield UserAction.ActionResult.NOT_HANDLED;
                }
                reset();
                yield UserAction.ActionResult.USED_STOP;
            }
            case ENTER -> {
                if (!hasSelection()) {
                    yield UserAction.ActionResult.NOT_HANDLED;
                }
                spawn();
                yield UserAction.ActionResult.USED_STOP;
            }
            default -> UserAction.ActionResult.NOT_HANDLED;
        };
    }

    private UserAction.ActionResult leftClick() {
        if (hasSelection() && (firstCorner.leftClick() || secondCorner.leftClick())) {
            return UserAction.ActionResult.USED_STOP;
        }
        RayCaster.RaycastResult hit = Tool.raycastBlock();
        if (hit == null) {
            return UserAction.ActionResult.NOT_HANDLED;
        }
        BlockPos pos = hit.getBlockPos();
        if (hasSelection()) {
            secondCorner.moveTo(pos);
        } else {
            firstCorner = new Gizmo(pos);
            secondCorner = new Gizmo(pos);
        }
        return UserAction.ActionResult.USED_STOP;
    }

    @Override
    public void render(AxiomWorldRenderContext context) {
        if (!hasSelection()) {
            RayCaster.RaycastResult hit = Tool.raycastBlock();
            if (hit != null) {
                Tool.renderRaycastOverlay(context, hit);
            }
            return;
        }

        Vec3 look = Tool.getLookDirection();
        boolean mouseDown = Tool.isMouseDown(0);
        boolean ctrl = EditorUI.isCtrlOrCmdDown();
        boolean editorActive = EditorUI.isActive();
        firstCorner.update(context.nanos(), look, mouseDown, ctrl, editorActive);
        secondCorner.update(context.nanos(), look, mouseDown, ctrl, editorActive);

        BlockPos min = min();
        BlockPos max = max();
        preview.render(
                context,
                style(),
                Vec3.atLowerCornerOf(min),
                Vec3.atLowerCornerOf(max).add(1, 1, 1));
        firstCorner.render(context, ctrl);
        secondCorner.render(context, ctrl);
    }

    @Override
    public void displayImguiOptions() {
        ImGui.inputText(Lang.t("markertools.name"), name);
        ImGui.colorEdit4(Lang.t("markertools.line"), lineColor);
        ImGui.colorEdit4(Lang.t("markertools.face"), faceColor);
        ImGui.sliderInt(Lang.t("markertools.thickness"), lineThickness, 1, 10);
        ImGui.separator();
        if (!hasSelection()) {
            ImGui.text(Lang.t("markertools.tool.hint"));
            return;
        }
        BlockPos min = min();
        BlockPos max = max();
        ImGui.text(
                Lang.t(
                        "markertools.size",
                                max.getX() - min.getX() + 1,
                                max.getY() - min.getY() + 1,
                                max.getZ() - min.getZ() + 1));
        if (ImGui.button(Lang.t("markertools.create"))) {
            spawn();
        }
    }

    private void spawn() {
        BlockPos min = min();
        MarkerStyle style =
                new MarkerStyle(
                        name.get(),
                        MarkerSettings.style().preset(),
                        toArgb(lineColor),
                        toArgb(faceColor),
                        lineThickness[0]);
        MarkerSettings.setStyle(style);
        MarkerPackets.spawn(Vec3.atLowerCornerOf(min), MarkerNbt.spawnTag(style, min, max()));
        reset();
    }

    private boolean hasSelection() {
        return firstCorner != null && secondCorner != null;
    }

    private BlockPos min() {
        BlockPos a = firstCorner.getTargetPosition();
        BlockPos b = secondCorner.getTargetPosition();
        return new BlockPos(
                Math.min(a.getX(), b.getX()),
                Math.min(a.getY(), b.getY()),
                Math.min(a.getZ(), b.getZ()));
    }

    private BlockPos max() {
        BlockPos a = firstCorner.getTargetPosition();
        BlockPos b = secondCorner.getTargetPosition();
        return new BlockPos(
                Math.max(a.getX(), b.getX()),
                Math.max(a.getY(), b.getY()),
                Math.max(a.getZ(), b.getZ()));
    }

    private BoundingBoxStyle style() {
        return new BoundingBoxStyle(
                lineThickness[0], toArgb(lineColor), false, 0, toArgb(faceColor));
    }

    @Override
    public String listenForEnter() {
        return hasSelection() ? Lang.t("markertools.create") : null;
    }

    @Override
    public String listenForEsc() {
        return hasSelection() ? Lang.t("gui.cancel") : null;
    }

    @Override
    public void reset() {
        firstCorner = null;
        secondCorner = null;
    }

    @Override
    public String name() {
        return Lang.t("markertools.marker");
    }

    @Override
    public void writeSettings(CompoundTag tag) {
        tag.putString("name", name.get());
        tag.putInt("line_argb", toArgb(lineColor));
        tag.putInt("face_argb", toArgb(faceColor));
        tag.putInt("line_thickness", lineThickness[0]);
    }

    @Override
    public void loadSettings(CompoundTag tag) {
        name.set(tag.getStringOr("name", ""));
        fromArgb(tag.getIntOr("line_argb", toArgb(lineColor)), lineColor);
        fromArgb(tag.getIntOr("face_argb", toArgb(faceColor)), faceColor);
        lineThickness[0] = tag.getIntOr("line_thickness", lineThickness[0]);
    }

    @Override
    public char iconChar() {
        return ICON;
    }

    /** No default keybind: Axiom skips tools whose keybind id is null, as it does for API tools. */
    @Override
    public String keybindId() {
        return null;
    }

    @Override
    public EnumSet<AxiomPermission> requiredPermissions() {
        return EnumSet.of(AxiomPermission.ENTITY_SPAWN);
    }

    /** ImGui RGBA floats (0..1) to a packed ARGB int, signed as NBT stores it. */
    private static int toArgb(float[] rgba) {
        int r = Math.round(rgba[0] * 255f);
        int g = Math.round(rgba[1] * 255f);
        int b = Math.round(rgba[2] * 255f);
        int a = Math.round(rgba[3] * 255f);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static void fromArgb(int argb, float[] rgba) {
        rgba[0] = ((argb >> 16) & 0xFF) / 255f;
        rgba[1] = ((argb >> 8) & 0xFF) / 255f;
        rgba[2] = (argb & 0xFF) / 255f;
        rgba[3] = ((argb >>> 24) & 0xFF) / 255f;
    }
}
