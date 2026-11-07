package io.github.italoseara.markertools;

import com.moulberry.axiom.buildertools.BuilderTool;
import com.moulberry.axiom.buildertools.BuilderToolManager;
import com.moulberry.axiom.tools.ToolManager;
import io.github.italoseara.markertools.mixin.BuilderToolManagerAccessor;
import io.github.italoseara.markertools.tool.MarkerBuilderTool;
import io.github.italoseara.markertools.tool.MarkerTool;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client entrypoint. Runs after Axiom's own initializer, so both of Axiom's tool lists already hold
 * the built-in tools and ours land at the end of each.
 */
public final class MarkerToolsClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("axiom-marker-tools");

    @Override
    public void onInitializeClient() {
        ToolManager.addTool(new MarkerTool());

        List<BuilderTool> builderTools = new ArrayList<>(BuilderToolManager.getTools());
        builderTools.add(MarkerBuilderTool.INSTANCE);
        BuilderToolManagerAccessor.markertools$setTools(List.copyOf(builderTools));

        LOGGER.info(
                "Marker tools registered: editor toolbar slot {}, builder tool slot {}",
                ToolManager.getTools().size(),
                MarkerBuilderTool.index() + 1);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> MarkerStore.clear());
    }
}
