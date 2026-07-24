package cn.suhoan.hypertransit.mixin;

import cn.suhoan.hypertransit.entity.ModBoatEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to modify boat speed for custom speed multipliers.
 * The controlBoat() method in AbstractBoat handles player input and applies acceleration.
 * We save the movement before controlBoat(), then scale the added acceleration by the multiplier.
 */
@Mixin(AbstractBoat.class)
public abstract class AbstractBoatMixin {

    @Unique
    private Vec3 hypertransit$preControlMovement;

    @Inject(method = "controlBoat()V", at = @At("HEAD"))
    private void hypertransit$savePreMovement(CallbackInfo ci) {
        this.hypertransit$preControlMovement = ((Entity) (Object) this).getDeltaMovement();
    }

    @Inject(method = "controlBoat()V", at = @At("TAIL"))
    private void hypertransit$scaleMovement(CallbackInfo ci) {
        if ((Object) this instanceof ModBoatEntity modBoat) {
            float mult = modBoat.getSpeedMultiplier();
            if (mult != 1.0F && this.hypertransit$preControlMovement != null) {
                Entity self = (Entity) (Object) this;
                Vec3 current = self.getDeltaMovement();
                Vec3 pre = this.hypertransit$preControlMovement;
                // Scale only the acceleration added by controlBoat()
                double ax = (current.x - pre.x) * mult;
                double ay = current.y - pre.y;
                double az = (current.z - pre.z) * mult;
                self.setDeltaMovement(new Vec3(pre.x + ax, pre.y + ay, pre.z + az));
            }
        }
    }
}
