package cn.suhoan.hypertransit;

import cn.suhoan.hypertransit.entity.ModEntityTypes;
import cn.suhoan.hypertransit.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HyperTransit implements ModInitializer {
    public static final String MOD_ID = "hypertransit";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("HyperTransit initializing - faster boats and minecarts!");
        ModEntityTypes.register();
        ModItems.register();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
