package io.github.italoseara.markertools;

import com.moulberry.axiom.marker.MarkerData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Axiom hides a marker once the camera is further than "Marker Entity Range" from the marker's
 * origin, so a large region vanishes while you stand inside it. This measures to the nearest point
 * of the marker's box instead.
 */
public final class MarkerRange {

    private static final double SAME_POSITION_SQR = 1.0E-6;

    private MarkerRange() {}

    /**
     * Squared distance from {@code camera} to the box of the marker whose gizmo sits at {@code
     * origin}; falls back to the origin when no known marker sits there (e.g. a gizmo mid-move) or
     * the marker is a point with no region.
     */
    public static double distanceSqr(Vec3 origin, Vec3 camera) {
        for (MarkerData marker : MarkerStore.all()) {
            if (marker.minRegion() != null
                    && marker.maxRegion() != null
                    && marker.position().distanceToSqr(origin) < SAME_POSITION_SQR) {
                return new AABB(marker.minRegion(), marker.maxRegion()).distanceToSqr(camera);
            }
        }
        return origin.distanceToSqr(camera);
    }
}
