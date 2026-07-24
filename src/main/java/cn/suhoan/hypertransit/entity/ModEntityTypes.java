package cn.suhoan.hypertransit.entity;

import cn.suhoan.hypertransit.HyperTransit;
import cn.suhoan.hypertransit.item.ModItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Registers all custom entity types for HyperTransit.
 */
public class ModEntityTypes {
    // Boat entity types
    public static final EntityType<ModBoatEntity> IRON_BOAT = registerBoat("iron_boat", 1.5F);
    public static final EntityType<ModBoatEntity> COPPER_BOAT = registerBoat("copper_boat", 2.0F);
    public static final EntityType<ModBoatEntity> GOLD_BOAT = registerBoat("gold_boat", 2.5F);
    public static final EntityType<ModBoatEntity> DIAMOND_BOAT = registerBoat("diamond_boat", 3.0F);
    public static final EntityType<ModBoatEntity> NETHERITE_BOAT = registerBoat("netherite_boat", 3.5F);

    // Minecart entity types
    public static final EntityType<ModMinecartEntity> COPPER_MINECART = registerMinecart("copper_minecart", 1.5F);
    public static final EntityType<ModMinecartEntity> GOLD_MINECART = registerMinecart("gold_minecart", 2.0F);
    public static final EntityType<ModMinecartEntity> DIAMOND_MINECART = registerMinecart("diamond_minecart", 3.0F);
    public static final EntityType<ModMinecartEntity> NETHERITE_MINECART = registerMinecart("netherite_minecart", 4.0F);

    private static EntityType<ModBoatEntity> registerBoat(String name, float speedMultiplier) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, HyperTransit.id(name));
        EntityType<ModBoatEntity> type = EntityType.Builder.<ModBoatEntity>of(
                        (entityType, level) -> {
                            ModBoatEntity boat = new ModBoatEntity(entityType, level, () -> ModItems.getBoatItem(name));
                            boat.setSpeedMultiplier(speedMultiplier);
                            return boat;
                        },
                        MobCategory.MISC)
                .sized(1.375F, 0.5625F)
                .eyeHeight(0.5625F)
                .clientTrackingRange(10)
                .build(key);
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
    }

    private static EntityType<ModMinecartEntity> registerMinecart(String name, float speedMultiplier) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, HyperTransit.id(name));
        EntityType<ModMinecartEntity> type = EntityType.Builder.<ModMinecartEntity>of(
                        (entityType, level) -> {
                            ModMinecartEntity minecart = new ModMinecartEntity(entityType, level, () -> ModItems.getMinecartItem(name));
                            minecart.setSpeedMultiplier(speedMultiplier);
                            return minecart;
                        },
                        MobCategory.MISC)
                .sized(0.98F, 0.7F)
                .eyeHeight(0.7F)
                .clientTrackingRange(8)
                .build(key);
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
    }

    public static void register() {
        // Entity types are registered via static initialization
        HyperTransit.LOGGER.info("Registered HyperTransit entity types");
    }

    public static float getBoatSpeedMultiplier(String name) {
        return switch (name) {
            case "iron_boat" -> 1.5F;
            case "copper_boat" -> 2.0F;
            case "gold_boat" -> 2.5F;
            case "diamond_boat" -> 3.0F;
            case "netherite_boat" -> 3.5F;
            default -> 1.0F;
        };
    }

    public static float getMinecartSpeedMultiplier(String name) {
        return switch (name) {
            case "copper_minecart" -> 1.5F;
            case "gold_minecart" -> 2.0F;
            case "diamond_minecart" -> 3.0F;
            case "netherite_minecart" -> 4.0F;
            default -> 1.0F;
        };
    }
}
