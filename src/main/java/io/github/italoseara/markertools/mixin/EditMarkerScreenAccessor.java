package io.github.italoseara.markertools.mixin;

import com.moulberry.axiom.screen.EditMarkerScreen;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EditMarkerScreen.class, remap = false)
public interface EditMarkerScreenAccessor {

    @Accessor("editingEntityUuid")
    UUID markertools$uuid();

    @Accessor("data")
    CompoundTag markertools$data();
}
