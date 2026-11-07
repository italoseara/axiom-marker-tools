package io.github.italoseara.markertools.mixin;

import com.moulberry.axiom.screen.SwitchHotbarScreen;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = SwitchHotbarScreen.class, remap = false)
public interface SwitchHotbarScreenAccessor {

    @Accessor("optionsOpen")
    boolean markertools$optionsOpen();

    @Accessor("optionsWidgets")
    List<AbstractWidget> markertools$optionsWidgets();
}
