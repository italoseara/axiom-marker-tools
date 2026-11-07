package io.github.italoseara.markertools.mixin;

import com.moulberry.axiom.marker.MarkerData;
import com.moulberry.axiom.marker.MarkerEntityManipulator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MarkerEntityManipulator.class, remap = false)
public interface MarkerEntityManipulatorAccessor {

    @Accessor("activeEntity")
    static MarkerData markertools$activeEntity() {
        throw new AssertionError();
    }
}
