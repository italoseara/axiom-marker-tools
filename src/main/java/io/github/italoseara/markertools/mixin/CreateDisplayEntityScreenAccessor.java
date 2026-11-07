package io.github.italoseara.markertools.mixin;

import com.moulberry.axiom.screen.CreateDisplayEntityScreen;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CreateDisplayEntityScreen.class, remap = false)
public interface CreateDisplayEntityScreenAccessor {

    @Accessor("leftPos")
    int markertools$leftPos();

    @Accessor("topPos")
    int markertools$topPos();

    @Accessor("canSwitchType")
    boolean markertools$canSwitchType();

    @Accessor("screenType")
    int markertools$screenType();

    @Accessor("screenType")
    void markertools$setScreenType(int screenType);

    @Accessor("TAB_ITEM_STACKS")
    static ItemStack[] markertools$tabItemStacks() {
        throw new AssertionError();
    }
}
