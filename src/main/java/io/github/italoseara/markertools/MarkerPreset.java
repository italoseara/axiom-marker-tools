package io.github.italoseara.markertools;

import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * A starting point for a marker: default colors plus the {@code type} written into the marker's
 * {@code data}, which server plugins read to know what the region is for.
 *
 * @param labelKey translation key of the name shown in the preset button
 * @param type value of {@code data.type}, or null to write none
 */
public record MarkerPreset(
        String labelKey, @Nullable String type, int lineArgb, int faceArgb, int lineThickness) {

    public static final MarkerPreset FREE = new MarkerPreset("markertools.preset.free", null, 0xFFFFFFFF, 0x20FFFFFF, 2);

    public static final List<MarkerPreset> BUILT_IN =
            List.of(
                    FREE,
                    new MarkerPreset("markertools.preset.mob_spawn", "mob_spawn", 0xFFFF5555, 0x33FF5555, 2),
                    new MarkerPreset("markertools.preset.event_region", "event_region", 0xFF55AAFF, 0x3355AAFF, 2),
                    new MarkerPreset("markertools.preset.npc", "npc", 0xFF55FF55, 0x3355FF55, 2));

    /** The preset whose {@code type} matches, or {@link #FREE}. */
    public static MarkerPreset byType(@Nullable String type) {
        for (MarkerPreset preset : BUILT_IN) {
            if (preset.type() != null && preset.type().equals(type)) {
                return preset;
            }
        }
        return FREE;
    }
}
