package io.github.italoseara.markertools.tool;

import com.moulberry.axiom.editor.EditorUI;
import com.moulberry.axiom.gizmo.Gizmo;
import com.moulberry.axiom.marker.MarkerData;
import com.moulberry.axiom.render.AxiomWorldRenderContext;
import com.moulberry.axiom.render.renderable.BoundingBoxStyle;
import com.moulberry.axiom.render.renderable.RenderableBoundingBox;
import com.moulberry.axiom.tools.Tool;
import io.github.italoseara.markertools.axiom.MarkerNbt;
import io.github.italoseara.markertools.axiom.MarkerPackets;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Two corner gizmos added to the marker Axiom has selected (right-click a marker), the same gizmos
 * the Box Select tool uses to resize a selection. Dragging previews the new box; releasing writes
 * the new {@code min}/{@code max} through Axiom's manipulate packet.
 *
 * <p>Corners snap to whole blocks: a box with fractional corners becomes the smallest block-aligned
 * box around it on its first resize.
 */
public final class MarkerResizeHandles {

    private static final RenderableBoundingBox PREVIEW = new RenderableBoundingBox();

    @Nullable private static UUID markerUuid;
    @Nullable private static MarkerData lastData;
    @Nullable private static Gizmo minCorner;
    @Nullable private static Gizmo maxCorner;
    private static boolean wasGrabbed;

    private MarkerResizeHandles() {}

    /** Called after Axiom draws its marker gizmos; {@code active} is Axiom's selected marker. */
    public static void render(AxiomWorldRenderContext context, @Nullable MarkerData active) {
        if (active == null || active.minRegion() == null || active.maxRegion() == null) {
            clear();
            return;
        }
        if (!active.uuid().equals(markerUuid) || (!isGrabbed() && !active.equals(lastData))) {
            attach(active);
        }

        Vec3 look = Tool.getLookDirection();
        boolean mouseDown = Tool.isMouseDown(0);
        boolean ctrl = EditorUI.isCtrlOrCmdDown();
        boolean editorActive = EditorUI.isActive();
        minCorner.update(context.nanos(), look, mouseDown, ctrl, editorActive);
        maxCorner.update(context.nanos(), look, mouseDown, ctrl, editorActive);

        boolean grabbed = isGrabbed();
        if (grabbed) {
            BlockPos min = BlockPos.min(minCorner.getTargetPosition(), maxCorner.getTargetPosition());
            BlockPos max = BlockPos.max(minCorner.getTargetPosition(), maxCorner.getTargetPosition());
            BoundingBoxStyle style =
                    new BoundingBoxStyle(
                            Math.max(1f, active.lineThickness()),
                            active.lineArgb(),
                            false,
                            0,
                            active.faceArgb());
            PREVIEW.render(
                    context, style, Vec3.atLowerCornerOf(min), Vec3.atLowerCornerOf(max).add(1, 1, 1));
        } else if (wasGrabbed) {
            commit(active);
        }
        wasGrabbed = grabbed;

        minCorner.render(context, ctrl);
        maxCorner.render(context, ctrl);
    }

    /** True when the click grabbed one of our corners, so Axiom should not handle it. */
    public static boolean leftClick() {
        if (minCorner == null || maxCorner == null) {
            return false;
        }
        return minCorner.leftClick() || maxCorner.leftClick();
    }

    private static void attach(MarkerData data) {
        markerUuid = data.uuid();
        lastData = data;
        Vec3 lo = data.minRegion();
        Vec3 hi = data.maxRegion();
        BlockPos min = BlockPos.containing(Math.min(lo.x, hi.x), Math.min(lo.y, hi.y), Math.min(lo.z, hi.z));
        BlockPos max =
                new BlockPos(
                        Math.max(min.getX(), Mth.ceil(Math.max(lo.x, hi.x)) - 1),
                        Math.max(min.getY(), Mth.ceil(Math.max(lo.y, hi.y)) - 1),
                        Math.max(min.getZ(), Mth.ceil(Math.max(lo.z, hi.z)) - 1));
        minCorner = new Gizmo(min);
        maxCorner = new Gizmo(max);
        wasGrabbed = false;
    }

    private static void commit(MarkerData data) {
        BlockPos min = BlockPos.min(minCorner.getTargetPosition(), maxCorner.getTargetPosition());
        BlockPos max = BlockPos.max(minCorner.getTargetPosition(), maxCorner.getTargetPosition());
        Vec3 origin = data.position();
        CompoundTag box = new CompoundTag();
        box.put("min", MarkerNbt.relative(min.getX() - origin.x, min.getY() - origin.y, min.getZ() - origin.z));
        box.put(
                "max",
                MarkerNbt.relative(
                        max.getX() + 1 - origin.x, max.getY() + 1 - origin.y, max.getZ() + 1 - origin.z));
        MarkerPackets.mergeData(data.uuid(), box);
    }

    private static boolean isGrabbed() {
        return minCorner != null && maxCorner != null && (minCorner.isGrabbed() || maxCorner.isGrabbed());
    }

    private static void clear() {
        markerUuid = null;
        lastData = null;
        minCorner = null;
        maxCorner = null;
        wasGrabbed = false;
    }
}
