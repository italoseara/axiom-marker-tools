package io.github.italoseara.markertools.axiom;

import com.moulberry.axiom.packets.AxiomServerboundManipulateEntity;
import com.moulberry.axiom.packets.AxiomServerboundSpawnEntity;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

/**
 * Marker writes through Axiom's own entity packets, so AxiomPaper's {@code axiom.entity.*}
 * permissions and region integrations decide whether they go through.
 */
public final class MarkerPackets {

    private MarkerPackets() {}

    public static void spawn(Vec3 position, CompoundTag tag) {
        AxiomServerboundSpawnEntity.SpawnEntry entry =
                new AxiomServerboundSpawnEntity.SpawnEntry(
                        UUID.randomUUID(), position, 0f, 0f, null, tag);
        new AxiomServerboundSpawnEntity(List.of(entry)).send();
    }

    /** Merges {@code data} into the marker's {@code data} compound; absent keys are kept. */
    public static void mergeData(UUID marker, CompoundTag data) {
        CompoundTag tag = new CompoundTag();
        tag.put("data", data);
        AxiomServerboundManipulateEntity.ManipulateEntry entry =
                new AxiomServerboundManipulateEntity.ManipulateEntry(marker, null, tag);
        new AxiomServerboundManipulateEntity(List.of(entry)).send();
    }
}
