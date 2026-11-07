package io.github.italoseara.markertools.mixin;

import com.moulberry.axiom.UserAction;
import com.moulberry.axiom.marker.MarkerEntityManipulator;
import com.moulberry.axiom.render.AxiomWorldRenderContext;
import io.github.italoseara.markertools.MarkerRange;
import io.github.italoseara.markertools.tool.MarkerResizeHandles;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds corner resize gizmos to the marker Axiom has selected, and makes the marker range measure
 * to the marker's box rather than its origin (drawing and clicking alike).
 */
@Mixin(value = MarkerEntityManipulator.class, remap = false)
public abstract class MarkerEntityManipulatorMixin {

    private static final String DISTANCE_TO_SQR =
            "Lnet/minecraft/world/phys/Vec3;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D";

    @Inject(method = "render", at = @At("TAIL"))
    private static void markertools$renderResizeHandles(AxiomWorldRenderContext context, CallbackInfo ci) {
        MarkerResizeHandles.render(context, MarkerEntityManipulatorAccessor.markertools$activeEntity());
    }

    @Inject(method = "handleLeftClick", at = @At("HEAD"), cancellable = true)
    private static void markertools$grabResizeHandle(CallbackInfoReturnable<UserAction.ActionResult> cir) {
        if (MarkerResizeHandles.leftClick()) {
            cir.setReturnValue(UserAction.ActionResult.USED_STOP);
        }
    }

    @Redirect(
            method = {"render", "handleLeftClick", "handleRightClick"},
            at = @At(value = "INVOKE", target = DISTANCE_TO_SQR, remap = true),
            require = 3)
    private static double markertools$distanceToBox(Vec3 origin, Vec3 camera) {
        return MarkerRange.distanceSqr(origin, camera);
    }
}
