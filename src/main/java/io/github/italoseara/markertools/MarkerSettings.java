package io.github.italoseara.markertools;

/**
 * The last style and size the player used, shared by every way of creating a marker (toolbar tool,
 * builder tool, display tab) so a new marker starts where the previous one left off.
 */
public final class MarkerSettings {

    private static MarkerStyle style = MarkerStyle.of(MarkerPreset.FREE);
    private static final int[] SIZE = {3, 3, 3};

    private MarkerSettings() {}

    public static MarkerStyle style() {
        return style;
    }

    public static void setStyle(MarkerStyle style) {
        MarkerSettings.style = style;
    }

    /** Width, height, depth of a marker created without a selection. */
    public static int[] size() {
        return SIZE;
    }
}
