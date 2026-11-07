package io.github.italoseara.markertools;

import net.minecraft.client.resources.language.I18n;

/** Translated plain strings, for the places that take a String instead of a Component (ImGui, Axiom tools). */
public final class Lang {

    private Lang() {}

    public static String t(String key, Object... args) {
        return I18n.get(key, args);
    }
}
