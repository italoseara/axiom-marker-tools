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
import io.github.italoseara.markertools.mixin.MarkerEntityManipulatorAccessor;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Corner handles for resizing the marker Axiom has selected (right-click a marker), set up like the
 * Box Select tool's corners:
 *
 * <ul>
 *   <li>each handle sits on the outer corner of the box and its arrows point away from the box;
 *   <li>only one gizmo shows its arrows at a time. Axiom's move gizmo on the marker's origin starts
 *       active, and clicking a corner's center box activates that corner instead. Clicking the
 *       origin, when a corner sits on it, switches between moving the marker and resizing.
 * </ul>
 *
 * <p>Dragging previews the new box; releasing writes the new {@code min}/{@code max} through
 * Axiom's manipulate packet. Corners snap to whole blocks: a box with fractional corners becomes
 * the smallest block-aligned box around it on its first resize.
 */
public final class MarkerResizeHandles {

    private static final RenderableBoundingBox PREVIEW = new RenderableBoundingBox();
    private static final double SAME_POINT_SQR = 1.0E-4;

    @Nullable private static UUID markerUuid;
    @Nullable private static MarkerData lastData;
    @Nullable private static Gizmo firstCorner;
    @Nullable private static Gizmo secondCorner;
    private static boolean wasGrabbed;

    private MarkerResizeHandles() {}

    /** Called after Axiom draws its marker gizmos; {@code active} is Axiom's selected marker. */
    public static void render(AxiomWorldRenderContext context, @Nullable MarkerData active) {
        if (active == null || active.minRegion() == null || active.maxRegion() == null) {
            clear();
            return;
        }
        if (!active.uuid().equals(markerUuid) || (!isGrabbed() && !active.equals(lastData))) {
            int activeCorner = active.uuid().equals(markerUuid) ? activeCorner() : -1;
            attach(active);
            // A resize just landed: keep the same corner active instead of jumping back to move.
            if (activeCorner == 0) {
                firstCorner.enableAxes = true;
            } else if (activeCorner == 1) {
                secondCorner.enableAxes = true;
            }
        }
        orientCorners();

        Vec3 look = Tool.getLookDirection();
        boolean editorActive = EditorUI.isActive();
        // Same buttons as Axiom's marker gizmos: left in the editor, right in game.
        boolean mouseDown = Tool.isMouseDown(editorActive ? 0 : 1);
        boolean ctrl = EditorUI.isCtrlOrCmdDown();
        // The last argument enables hovering and grabbing; Axiom passes true for marker gizmos in
        // game too, and false here would make the corners ignore the cursor outside the editor.
        firstCorner.update(context.nanos(), look, mouseDown, ctrl, true);
        secondCorner.update(context.nanos(), look, mouseDown, ctrl, true);

        boolean grabbed = isGrabbed();
        if (grabbed) {
            BlockPos min = BlockPos.min(firstCorner.getTargetPosition(), secondCorner.getTargetPosition());
            BlockPos max = BlockPos.max(firstCorner.getTargetPosition(), secondCorner.getTargetPosition());
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

        // A corner on the origin would draw a second center box over Axiom's; skip it while idle.
        if (firstCorner.enableAxes || !onOrigin(firstCorner, active)) {
            firstCorner.render(context, ctrl);
        }
        if (secondCorner.enableAxes || !onOrigin(secondCorner, active)) {
            secondCorner.render(context, ctrl);
        }
    }

    /**
     * Handles a click before Axiom does. Axiom grabs marker gizmos with the left button in the
     * editor and the right button in game, so only that button counts here. Returns true when the
     * click belongs to a corner (or toggles between moving and resizing), so Axiom should not
     * handle it; otherwise Axiom's right click would select another marker or deselect this one.
     */
    public static boolean click(boolean rightButton) {
        MarkerData active = MarkerEntityManipulatorAccessor.markertools$activeEntity();
        if (firstCorner == null || secondCorner == null || active == null) {
            return false;
        }
        if (rightButton == EditorUI.isActive()) {
            return false;
        }
        Gizmo move = MarkerEntityManipulatorAccessor.markertools$gizmos().get(active.uuid());

        // Clicking the origin's center box while resizing from a corner on it goes back to moving.
        for (Gizmo corner : new Gizmo[] {firstCorner, secondCorner}) {
            if (corner.enableAxes && onOrigin(corner, active) && corner.isCenterHovered() && move != null) {
                corner.enableAxes = false;
                move.enableAxes = true;
                return true;
            }
        }

        for (Gizmo corner : new Gizmo[] {firstCorner, secondCorner}) {
            boolean wasActive = corner.enableAxes;
            if (corner.leftClick()) {
                if (!wasActive) {
                    Gizmo other = corner == firstCorner ? secondCorner : firstCorner;
                    other.enableAxes = false;
                    if (move != null) {
                        move.enableAxes = false;
                    }
                }
                return true;
            }
        }

        // Axiom's gizmo is about to take the click: hand the arrows back to it.
        if (move != null && move.isHovered()) {
            firstCorner.enableAxes = false;
            secondCorner.enableAxes = false;
        }
        return false;
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
        firstCorner = new Gizmo(min, new Vec3(-0.5, -0.5, -0.5));
        secondCorner = new Gizmo(max, new Vec3(0.5, 0.5, 0.5));
        firstCorner.enableAxes = false;
        secondCorner.enableAxes = false;
        int handleColour = handleColour(data.lineArgb());
        firstCorner.minVisualScale = 2f;
        secondCorner.minVisualScale = 2f;
        firstCorner.centerColour = handleColour;
        secondCorner.centerColour = handleColour;
        wasGrabbed = false;
    }

    /**
     * Keeps each corner on the outer corner of the box with its arrows pointing outwards, also after
     * one corner is dragged past the other.
     */
    private static void orientCorners() {
        BlockPos a = firstCorner.getTargetPosition();
        BlockPos b = secondCorner.getTargetPosition();
        boolean x = a.getX() > b.getX();
        boolean y = a.getY() > b.getY();
        boolean z = a.getZ() > b.getZ();
        Vec3 offset = new Vec3(x ? 0.5 : -0.5, y ? 0.5 : -0.5, z ? 0.5 : -0.5);
        if (!offset.equals(firstCorner.getOffset())) {
            firstCorner.setOffset(offset);
            secondCorner.setOffset(offset.scale(-1));
        }
        firstCorner.setAxisDirections(x, y, z);
        secondCorner.setAxisDirections(!x, !y, !z);
    }

    /**
     * The marker's line color as the plain RGB Axiom expects for a gizmo center, swapped for white
     * when it is too dark to see against the world.
     */
    private static int handleColour(int lineArgb) {
        int r = (lineArgb >> 16) & 0xFF;
        int g = (lineArgb >> 8) & 0xFF;
        int b = lineArgb & 0xFF;
        boolean dark = 0.299 * r + 0.587 * g + 0.114 * b < 110;
        return dark ? 0xFFFFFF : lineArgb & 0xFFFFFF;
    }

    /** Whether the corner's handle, block center plus offset, sits on the marker's origin. */
    private static boolean onOrigin(Gizmo corner, MarkerData data) {
        return corner.getTargetVec().add(corner.getOffset()).distanceToSqr(data.position())
                < SAME_POINT_SQR;
    }

    /** 0 or 1 for the corner showing its arrows, -1 when neither does. */
    private static int activeCorner() {
        if (firstCorner == null || secondCorner == null) {
            return -1;
        }
        return firstCorner.enableAxes ? 0 : secondCorner.enableAxes ? 1 : -1;
    }

    private static void commit(MarkerData data) {
        BlockPos min = BlockPos.min(firstCorner.getTargetPosition(), secondCorner.getTargetPosition());
        BlockPos max = BlockPos.max(firstCorner.getTargetPosition(), secondCorner.getTargetPosition());
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
        return firstCorner != null
                && secondCorner != null
                && (firstCorner.isGrabbed() || secondCorner.isGrabbed());
    }

    private static void clear() {
        markerUuid = null;
        lastData = null;
        firstCorner = null;
        secondCorner = null;
        wasGrabbed = false;
    }
}
