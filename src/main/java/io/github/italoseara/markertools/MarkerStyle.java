package io.github.italoseara.markertools;

/** How a marker looks and which preset it came from. Colors are packed ARGB. */
public record MarkerStyle(
        String name, MarkerPreset preset, int lineArgb, int faceArgb, int lineThickness) {

    public MarkerStyle withName(String name) {
        return new MarkerStyle(name, preset, lineArgb, faceArgb, lineThickness);
    }

    public MarkerStyle withPreset(MarkerPreset preset) {
        return new MarkerStyle(
                name, preset, preset.lineArgb(), preset.faceArgb(), preset.lineThickness());
    }

    public MarkerStyle withLine(int lineArgb) {
        return new MarkerStyle(name, preset, lineArgb, faceArgb, lineThickness);
    }

    public MarkerStyle withFace(int faceArgb) {
        return new MarkerStyle(name, preset, lineArgb, faceArgb, lineThickness);
    }

    public MarkerStyle withThickness(int lineThickness) {
        return new MarkerStyle(name, preset, lineArgb, faceArgb, lineThickness);
    }

    public static MarkerStyle of(MarkerPreset preset) {
        return new MarkerStyle(
                "", preset, preset.lineArgb(), preset.faceArgb(), preset.lineThickness());
    }
}
