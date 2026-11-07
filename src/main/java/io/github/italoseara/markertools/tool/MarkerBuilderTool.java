package io.github.italoseara.markertools.tool;

import io.github.italoseara.markertools.Lang;
import com.mojang.blaze3d.platform.InputConstants;
import com.moulberry.axiom.buildertools.BuilderTool;
import com.moulberry.axiom.buildertools.BuilderToolManager;
import com.moulberry.axiom.render.AxiomWorldRenderContext;
import com.moulberry.axiom.render.renderable.BoundingBoxStyle;
import com.moulberry.axiom.render.renderable.RenderableBoundingBox;
import com.moulberry.axiom.restrictions.AxiomPermission;
import io.github.italoseara.markertools.MarkerSettings;
import io.github.italoseara.markertools.MarkerStyle;
import io.github.italoseara.markertools.screen.MarkerCreateScreen;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * Builder tool (the slot beside the hotbar) for markers. Like Axiom's Erase tool: left click sets
 * the first corner, right click the second. Enter opens the create screen with the box fixed to
 * the selection.
 */
public final class MarkerBuilderTool implements BuilderTool {

    public static final MarkerBuilderTool INSTANCE = new MarkerBuilderTool();

    private final RenderableBoundingBox preview = new RenderableBoundingBox();
    @Nullable private BlockPos first;
    @Nullable private BlockPos second;
    private boolean enterWasDown;

    private MarkerBuilderTool() {}

    /** This tool's slot in Axiom's builder tool list, or -1 before registration. */
    public static int index() {
        return BuilderToolManager.getTools().indexOf(INSTANCE);
    }

    @Override
    public void renderScreen(GuiGraphics graphics, int x, int y, float delta) {
        Minecraft minecraft = Minecraft.getInstance();
        BuilderTool.renderKeybindHelp(graphics, x, y, Lang.t("markertools.first_corner"), minecraft.options.keyAttack, 0);
        BuilderTool.renderKeybindHelp(graphics, x, y, Lang.t("markertools.second_corner"), minecraft.options.keyUse, 1);
        if (first != null && second != null) {
            String hint = "[ENTER] " + Lang.t("markertools.create");
            int width = minecraft.font.width(hint);
            graphics.drawString(minecraft.font, hint, x - width / 2, y - 3 * 12, 0xFFFFFFFF);
        }
    }

    @Override
    public void renderWorld(AxiomWorldRenderContext context) {
        if (first == null) {
            return;
        }
        BlockPos other = second != null ? second : first;
        BlockPos min = BlockPos.min(first, other);
        BlockPos max = BlockPos.max(first, other);
        MarkerStyle style = MarkerSettings.style();
        preview.render(
                context,
                new BoundingBoxStyle(
                        Math.max(1, style.lineThickness()), style.lineArgb(), false, 0, style.faceArgb()),
                Vec3.atLowerCornerOf(min),
                Vec3.atLowerCornerOf(max).add(1, 1, 1));

        Minecraft minecraft = Minecraft.getInstance();
        boolean enterDown =
                minecraft.screen == null
                        && InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_ENTER);
        if (enterDown && !enterWasDown && second != null) {
            minecraft.setScreen(MarkerCreateScreen.forSelection(first, second));
            reset(false);
        }
        enterWasDown = enterDown;
    }

    @Override
    public void leftClick(HitResult hit) {
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            first = blockHit.getBlockPos();
        }
    }

    @Override
    public void rightClick(HitResult hit) {
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            second = blockHit.getBlockPos();
            if (first == null) {
                first = second;
            }
        }
    }

    @Override
    public boolean setPos1(BlockPos pos) {
        first = pos;
        return true;
    }

    @Override
    public boolean setPos2(BlockPos pos) {
        second = pos;
        return true;
    }

    @Override
    public boolean scroll(int amount) {
        return false;
    }

    @Override
    public boolean shouldRenderBlockOutline(BlockPos pos) {
        return true;
    }

    @Override
    public String getName() {
        return Lang.t("markertools.marker");
    }

    @Override
    public List<String> getKeyHints() {
        return List.of(
                "LMB - " + Lang.t("markertools.first_corner"),
                "RMB - " + Lang.t("markertools.second_corner"),
                "Enter - " + Lang.t("markertools.create"));
    }

    @Override
    public boolean canBeReset() {
        return first != null;
    }

    @Override
    public void reset(boolean force) {
        first = null;
        second = null;
    }

    @Override
    public EnumSet<AxiomPermission> requiredPermissions() {
        return EnumSet.of(AxiomPermission.ENTITY_SPAWN);
    }
}
