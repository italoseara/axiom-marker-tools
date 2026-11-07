package io.github.italoseara.markertools.mixin;

import com.moulberry.axiom.packets.AxiomClientboundMarkerData;
import io.github.italoseara.markertools.MarkerStore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Read-only tap on Axiom's marker packet: Axiom keeps handling it, we copy the delta. */
@Mixin(value = AxiomClientboundMarkerData.class, remap = false)
public abstract class MarkerDataPacketMixin {

    @Inject(method = "handle", at = @At("HEAD"))
    private void markertools$capture(CallbackInfo ci) {
        AxiomClientboundMarkerData self = (AxiomClientboundMarkerData) (Object) this;
        MarkerStore.apply(self.entries, self.removedMarkers);
    }
}
