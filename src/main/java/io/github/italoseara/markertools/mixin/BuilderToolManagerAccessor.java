package io.github.italoseara.markertools.mixin;

import com.moulberry.axiom.buildertools.BuilderTool;
import com.moulberry.axiom.buildertools.BuilderToolManager;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BuilderToolManager.class, remap = false)
public interface BuilderToolManagerAccessor {

    /** Axiom builds the list with {@code List.of}, so adding a tool means replacing it. */
    @Mutable
    @Accessor("tools")
    static void markertools$setTools(List<BuilderTool> tools) {
        throw new AssertionError();
    }
}
