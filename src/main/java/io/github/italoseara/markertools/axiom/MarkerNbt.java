package io.github.italoseara.markertools.axiom;

import io.github.italoseara.markertools.MarkerStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

/**
 * Marker NBT in the format AxiomPaper reads ({@code data.name/min/max/line_argb/line_thickness/
 * face_argb}). Boxes are written relative to the marker ({@code "~N"} strings) so they travel with
 * the marker through schematics.
 *
 * <p>Never emit a bare {@code "~"}: AxiomPaper parses it as {@code parseDouble("")}, gets NaN and
 * silently drops the whole box. {@link #formatRelative} always writes {@code "~0"} for zero.
 */
public final class MarkerNbt {

    private MarkerNbt() {}

    /**
     * The spawn tag for a marker placed at {@code min}'s lower corner whose box covers every block
     * from {@code min} to {@code max} inclusive.
     */
    public static CompoundTag spawnTag(MarkerStyle style, BlockPos min, BlockPos max) {
        CompoundTag data = styleData(style);
        data.put("min", relative(0, 0, 0));
        data.put(
                "max",
                relative(
                        max.getX() - min.getX() + 1,
                        max.getY() - min.getY() + 1,
                        max.getZ() - min.getZ() + 1));

        CompoundTag tag = new CompoundTag();
        tag.putString("id", "minecraft:marker");
        tag.put("data", data);
        return tag;
    }

    /** Name, colors, thickness and the preset's {@code type}, without the box. */
    public static CompoundTag styleData(MarkerStyle style) {
        CompoundTag data = new CompoundTag();
        data.putString("name", style.name());
        data.putInt("line_argb", style.lineArgb());
        data.putInt("line_thickness", style.lineThickness());
        data.putInt("face_argb", style.faceArgb());
        if (style.preset().type() != null) {
            data.putString("type", style.preset().type());
        }
        return data;
    }

    /** A box corner relative to the marker position. */
    public static ListTag relative(double x, double y, double z) {
        ListTag list = new ListTag();
        list.add(StringTag.valueOf(formatRelative(x)));
        list.add(StringTag.valueOf(formatRelative(y)));
        list.add(StringTag.valueOf(formatRelative(z)));
        return list;
    }

    public static String formatRelative(double value) {
        return "~" + formatNumber(value);
    }

    /** {@code 3} rather than {@code 3.0}, keeping real fractions like {@code 0.5}. */
    public static String formatNumber(double value) {
        if (value == Math.rint(value) && Math.abs(value) < 1e15) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }

    /**
     * Reads one corner of {@code data.min} or {@code data.max} as an offset from the marker,
     * accepting both forms AxiomPaper accepts: {@code "~N"} strings (relative) and doubles or plain
     * strings (absolute, converted with {@code markerPos}). Returns null when the corner is missing
     * or malformed.
     */
    public static Vec3 readCornerOffset(CompoundTag data, String key, Vec3 markerPos) {
        ListTag list = data.getListOrEmpty(key);
        if (list.size() != 3) {
            return null;
        }
        double[] out = new double[3];
        double[] origin = {markerPos.x, markerPos.y, markerPos.z};
        for (int i = 0; i < 3; i++) {
            Tag element = list.get(i);
            String text = element.asString().orElse(null);
            if (text != null) {
                try {
                    if (text.startsWith("~")) {
                        String rest = text.substring(1);
                        out[i] = rest.isEmpty() ? 0 : Double.parseDouble(rest);
                    } else {
                        out[i] = Double.parseDouble(text) - origin[i];
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            } else {
                Double number = element.asDouble().orElse(null);
                if (number == null) {
                    return null;
                }
                out[i] = number - origin[i];
            }
        }
        return new Vec3(out[0], out[1], out[2]);
    }
}
