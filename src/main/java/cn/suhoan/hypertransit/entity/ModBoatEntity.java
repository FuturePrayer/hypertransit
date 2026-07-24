package cn.suhoan.hypertransit.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.function.Supplier;

/**
 * A custom boat entity with configurable speed multiplier.
 * The speed multiplier affects the boat's acceleration in water and on land.
 */
public class ModBoatEntity extends Boat {
    private static final EntityDataAccessor<Float> DATA_SPEED_MULTIPLIER =
            SynchedEntityData.defineId(ModBoatEntity.class, EntityDataSerializers.FLOAT);

    public ModBoatEntity(EntityType<? extends Boat> type, Level level, Supplier<Item> dropItem) {
        super(type, level, dropItem);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SPEED_MULTIPLIER, 1.0F);
    }

    public float getSpeedMultiplier() {
        return this.entityData.get(DATA_SPEED_MULTIPLIER);
    }

    public void setSpeedMultiplier(float multiplier) {
        this.entityData.set(DATA_SPEED_MULTIPLIER, multiplier);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("SpeedMultiplier", this.getSpeedMultiplier());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSpeedMultiplier(input.getFloatOr("SpeedMultiplier", 1.0F));
    }
}
