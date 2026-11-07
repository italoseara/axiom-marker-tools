package io.github.italoseara.markertools.mixin;

import com.moulberry.axiom.gizmo.Gizmo;
import com.moulberry.axiom.marker.MarkerData;
import com.moulberry.axiom.marker.MarkerEntityManipulator;
import java.util.Map;
import java.util.UUID;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MarkerEntityManipulator.class, remap = false)
public interface MarkerEntityManipulatorAccessor {

    @Accessor("activeEntity")
    static MarkerData markertools$activeEntity() {
        throw new AssertionError();
    }

    /** Axiom's move gizmo of each marker, keyed by the marker's UUID. */
    @Accessor("gizmos")
    static Map<UUID, Gizmo> markertools$gizmos() {
        throw new AssertionError();
    }
}
