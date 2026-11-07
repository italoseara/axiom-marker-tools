package io.github.italoseara.markertools;

import com.moulberry.axiom.marker.MarkerData;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;

/**
 * Every marker the server has sent through AxiomPaper's {@code axiom:marker_data} packet, keyed by
 * entity UUID. AxiomPaper sends only the delta against its last state, so entries overwrite and
 * removals delete.
 */
public final class MarkerStore {

    private static final Map<UUID, MarkerData> MARKERS = new ConcurrentHashMap<>();

    private MarkerStore() {}

    public static void apply(List<MarkerData> entries, Set<UUID> removed) {
        for (MarkerData entry : entries) {
            MARKERS.put(entry.uuid(), entry);
        }
        removed.forEach(MARKERS::remove);
    }

    public static @Nullable MarkerData get(UUID uuid) {
        return MARKERS.get(uuid);
    }

    public static Collection<MarkerData> all() {
        return Collections.unmodifiableCollection(MARKERS.values());
    }

    public static void clear() {
        MARKERS.clear();
    }
}
