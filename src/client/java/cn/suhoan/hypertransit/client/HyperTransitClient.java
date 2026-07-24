package cn.suhoan.hypertransit.client;

import cn.suhoan.hypertransit.client.renderer.ModBoatRenderer;
import cn.suhoan.hypertransit.client.renderer.ModMinecartRenderer;
import cn.suhoan.hypertransit.entity.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * Client-side initializer for HyperTransit.
 * Registers entity renderers for custom boats and minecarts.
 */
public class HyperTransitClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register boat renderers
        EntityRendererRegistry.register(ModEntityTypes.IRON_BOAT, ctx -> new ModBoatRenderer(ctx, "iron_boat"));
        EntityRendererRegistry.register(ModEntityTypes.COPPER_BOAT, ctx -> new ModBoatRenderer(ctx, "copper_boat"));
        EntityRendererRegistry.register(ModEntityTypes.GOLD_BOAT, ctx -> new ModBoatRenderer(ctx, "gold_boat"));
        EntityRendererRegistry.register(ModEntityTypes.DIAMOND_BOAT, ctx -> new ModBoatRenderer(ctx, "diamond_boat"));
        EntityRendererRegistry.register(ModEntityTypes.NETHERITE_BOAT, ctx -> new ModBoatRenderer(ctx, "netherite_boat"));

        // Register minecart renderers
        EntityRendererRegistry.register(ModEntityTypes.COPPER_MINECART, ctx -> new ModMinecartRenderer(ctx, "copper_minecart"));
        EntityRendererRegistry.register(ModEntityTypes.GOLD_MINECART, ctx -> new ModMinecartRenderer(ctx, "gold_minecart"));
        EntityRendererRegistry.register(ModEntityTypes.DIAMOND_MINECART, ctx -> new ModMinecartRenderer(ctx, "diamond_minecart"));
        EntityRendererRegistry.register(ModEntityTypes.NETHERITE_MINECART, ctx -> new ModMinecartRenderer(ctx, "netherite_minecart"));
    }
}
