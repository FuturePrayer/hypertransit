package cn.suhoan.hypertransit.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

/**
 * A custom minecart entity with configurable speed multiplier.
 * The speed multiplier affects the minecart's max speed, which is based on
 * the gamerule minecartMaxSpeed (for new minecart behavior) or hardcoded values (for old behavior).
 */
public class ModMinecartEntity extends Minecart {
    private static final EntityDataAccessor<Float> DATA_SPEED_MULTIPLIER =
            SynchedEntityData.defineId(ModMinecartEntity.class, EntityDataSerializers.FLOAT);

    private final Supplier<Item> dropItem;

    public ModMinecartEntity(EntityType<?> type, Level level, Supplier<Item> dropItem) {
        super(type, level);
        this.dropItem = dropItem;
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
    protected double getMaxSpeed(ServerLevel level) {
        return super.getMaxSpeed(level) * this.getSpeedMultiplier();
    }

    /**
     * Override move to follow the track in sub-steps while preserving collision detection.
     * At high speeds, a single move() can skip past corner blocks into non-rail blocks.
     * We split large movements into sub-steps (max 0.4 blocks each), call super.move() for
     * each sub-step (retaining full collision), then snap back to rail center to prevent drift.
     */
    @Override
    public void move(MoverType moverType, Vec3 delta) {
        if (this.isOnRails() && moverType == MoverType.SELF) {
            double dist = delta.horizontalDistance();
            int steps = Math.max(1, (int) Math.ceil(dist / 0.4));
            Vec3 stepDelta = delta.scale(1.0 / steps);
            for (int i = 0; i < steps; i++) {
                super.move(moverType, stepDelta);
                if (this.isOnRails()) {
                    this.snapToRailCenter();
                }
            }
        } else {
            super.move(moverType, delta);
        }
    }

    /**
     * Snaps the minecart to the center line of the current rail to prevent lateral drift.
     * For straight rails, centers the lateral axis.
     * For corner rails, projects position onto the corner diagonal.
     * @return true if successfully snapped to a rail, false if not on any rail
     */
    private boolean snapToRailCenter() {
        BlockPos pos = this.getCurrentBlockPosOrRailBelow();
        BlockState state = this.level().getBlockState(pos);
        if (!(state.getBlock() instanceof BaseRailBlock railBlock)) return false;

        RailShape shape = state.getValue(railBlock.getShapeProperty());
        double cx = pos.getX() + 0.5;
        double cz = pos.getZ() + 0.5;
        double x = this.getX();
        double z = this.getZ();

        switch (shape) {
            case NORTH_SOUTH, ASCENDING_NORTH, ASCENDING_SOUTH -> this.setPos(cx, this.getY(), z);
            case EAST_WEST, ASCENDING_EAST, ASCENDING_WEST -> this.setPos(x, this.getY(), cz);
            case SOUTH_EAST -> {
                // Diagonal line: x + z = cx + cz + 0.5
                double sum = x + z;
                double targetSum = cx + cz + 0.5;
                double correction = (targetSum - sum) / 2.0;
                this.setPos(x + correction, this.getY(), z + correction);
            }
            case SOUTH_WEST -> {
                double diff = x - z;
                double targetDiff = cx - cz - 0.5;
                double correction = (targetDiff - diff) / 2.0;
                this.setPos(x + correction, this.getY(), z - correction);
            }
            case NORTH_WEST -> {
                double sum = x + z;
                double targetSum = cx + cz - 0.5;
                double correction = (targetSum - sum) / 2.0;
                this.setPos(x + correction, this.getY(), z + correction);
            }
            case NORTH_EAST -> {
                double diff = x - z;
                double targetDiff = cx - cz + 0.5;
                double correction = (targetDiff - diff) / 2.0;
                this.setPos(x + correction, this.getY(), z - correction);
            }
        }
        return true;
    }

    @Override
    protected Item getDropItem() {
        return this.dropItem.get();
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(this.dropItem.get());
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
