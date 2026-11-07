package io.github.italoseara.markertools.mixin;

import com.moulberry.axiom.VersionUtilsClient;
import com.moulberry.axiom.screen.SwitchBuilderToolScreen;
import io.github.italoseara.markertools.screen.AxiomGui;
import io.github.italoseara.markertools.tool.MarkerBuilderTool;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Axiom draws every builder tool icon from one atlas ({@code tool_swapper.png}): icon {@code i} at
 * u = 46 + 16i, and the slot column as one strip 2 + 20n pixels tall. The atlas only has seven
 * icons and seven slots, so for our eighth tool this draws our icon and repeats a slot row.
 */
@Mixin(value = VersionUtilsClient.class, remap = false)
public abstract class VersionUtilsClientMixin {

    private static final int ICON_U0 = 46;
    private static final int ICON_SIZE = 16;
    private static final int COLUMN_WIDTH = 22;
    private static final int SLOT_HEIGHT = 20;
    /** Height of the atlas column: a 2px top border plus seven slots. */
    private static final int ATLAS_COLUMN_HEIGHT = 2 + 7 * SLOT_HEIGHT;

    private static boolean markertools$drawing;

    @Inject(method = "blit256", at = @At("HEAD"), cancellable = true)
    private static void markertools$builderToolAtlas(
            GuiGraphics graphics,
            Identifier texture,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height,
            CallbackInfo ci) {
        if (markertools$drawing || !SwitchBuilderToolScreen.TOOL_SWAPPER_LOCATION.equals(texture)) {
            return;
        }
        int markerIndex = MarkerBuilderTool.index();
        if (markerIndex >= 0
                && v == 0
                && width == ICON_SIZE
                && height == ICON_SIZE
                && u == ICON_U0 + ICON_SIZE * markerIndex) {
            AxiomGui.markerIcon(graphics, x, y);
            ci.cancel();
            return;
        }
        if (u == 0 && v == 0 && width == COLUMN_WIDTH && height > ATLAS_COLUMN_HEIGHT) {
            markertools$drawing = true;
            try {
                VersionUtilsClient.blit256(graphics, texture, x, y, 0, 0, COLUMN_WIDTH, ATLAS_COLUMN_HEIGHT);
                for (int extraY = y + ATLAS_COLUMN_HEIGHT; extraY < y + height; extraY += SLOT_HEIGHT) {
                    VersionUtilsClient.blit256(
                            graphics,
                            texture,
                            x,
                            extraY,
                            0,
                            ATLAS_COLUMN_HEIGHT - SLOT_HEIGHT,
                            COLUMN_WIDTH,
                            Math.min(SLOT_HEIGHT, y + height - extraY));
                }
            } finally {
                markertools$drawing = false;
            }
            ci.cancel();
        }
    }
}
