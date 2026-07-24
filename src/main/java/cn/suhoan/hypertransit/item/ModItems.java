package cn.suhoan.hypertransit.item;

import cn.suhoan.hypertransit.HyperTransit;
import cn.suhoan.hypertransit.entity.ModEntityTypes;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MinecartItem;

import java.util.HashMap;
import java.util.Map;

/**
 * Registers all custom items for HyperTransit.
 */
public class ModItems {
    private static final Map<String, Item> BOAT_ITEMS = new HashMap<>();
    private static final Map<String, Item> MINECART_ITEMS = new HashMap<>();

    // Boat items
    public static final Item IRON_BOAT = registerBoatItem("iron_boat");
    public static final Item COPPER_BOAT = registerBoatItem("copper_boat");
    public static final Item GOLD_BOAT = registerBoatItem("gold_boat");
    public static final Item DIAMOND_BOAT = registerBoatItem("diamond_boat");
    public static final Item NETHERITE_BOAT = registerBoatItem("netherite_boat");

    // Minecart items
    public static final Item COPPER_MINECART = registerMinecartItem("copper_minecart");
    public static final Item GOLD_MINECART = registerMinecartItem("gold_minecart");
    public static final Item DIAMOND_MINECART = registerMinecartItem("diamond_minecart");
    public static final Item NETHERITE_MINECART = registerMinecartItem("netherite_minecart");

    private static Item registerBoatItem(String name) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, HyperTransit.id(name));
        Item item = new BoatItem(getBoatEntityType(name), new Item.Properties().stacksTo(1).setId(key));
        Item registered = Registry.register(BuiltInRegistries.ITEM, key, item);
        BOAT_ITEMS.put(name, registered);
        return registered;
    }

    private static Item registerMinecartItem(String name) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, HyperTransit.id(name));
        Item item = new MinecartItem(getMinecartEntityType(name), new Item.Properties().stacksTo(1).setId(key));
        Item registered = Registry.register(BuiltInRegistries.ITEM, key, item);
        MINECART_ITEMS.put(name, registered);
        return registered;
    }

    @SuppressWarnings("unchecked")
    private static net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.vehicle.boat.AbstractBoat> getBoatEntityType(String name) {
        return switch (name) {
            case "iron_boat" -> ModEntityTypes.IRON_BOAT;
            case "copper_boat" -> ModEntityTypes.COPPER_BOAT;
            case "gold_boat" -> ModEntityTypes.GOLD_BOAT;
            case "diamond_boat" -> ModEntityTypes.DIAMOND_BOAT;
            case "netherite_boat" -> ModEntityTypes.NETHERITE_BOAT;
            default -> throw new IllegalArgumentException("Unknown boat: " + name);
        };
    }

    @SuppressWarnings("unchecked")
    private static net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.vehicle.minecart.AbstractMinecart> getMinecartEntityType(String name) {
        return switch (name) {
            case "copper_minecart" -> ModEntityTypes.COPPER_MINECART;
            case "gold_minecart" -> ModEntityTypes.GOLD_MINECART;
            case "diamond_minecart" -> ModEntityTypes.DIAMOND_MINECART;
            case "netherite_minecart" -> ModEntityTypes.NETHERITE_MINECART;
            default -> throw new IllegalArgumentException("Unknown minecart: " + name);
        };
    }

    public static Item getBoatItem(String name) {
        return BOAT_ITEMS.get(name);
    }

    public static Item getMinecartItem(String name) {
        return MINECART_ITEMS.get(name);
    }

    public static void register() {
        // Items are registered via static initialization
        // Add items to creative mode tab (Tools & Utilities)
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register((tab) -> {
                    tab.accept(IRON_BOAT);
                    tab.accept(COPPER_BOAT);
                    tab.accept(GOLD_BOAT);
                    tab.accept(DIAMOND_BOAT);
                    tab.accept(NETHERITE_BOAT);
                    tab.accept(COPPER_MINECART);
                    tab.accept(GOLD_MINECART);
                    tab.accept(DIAMOND_MINECART);
                    tab.accept(NETHERITE_MINECART);
                });
        HyperTransit.LOGGER.info("Registered HyperTransit items");
    }
}
