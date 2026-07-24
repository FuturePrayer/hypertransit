package cn.suhoan.hypertransit.client.renderer;

import cn.suhoan.hypertransit.HyperTransit;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.cart.MinecartModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Custom minecart renderer that uses vanilla minecart model with custom textures.
 */
public class ModMinecartRenderer extends EntityRenderer<AbstractMinecart, MinecartRenderState> {
    private final Identifier texture;
    protected final MinecartModel model;

    public ModMinecartRenderer(EntityRendererProvider.Context context, String minecartType) {
        super(context);
        this.shadowRadius = 0.7F;
        this.texture = HyperTransit.id("textures/entity/minecart/" + minecartType + ".png");
        this.model = new MinecartModel(context.bakeLayer(ModelLayers.MINECART));
    }

    @Override
    public void submit(MinecartRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
        poseStack.pushPose();
        long seed = state.offsetSeed;
        float offsetX = (((float)(seed >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float offsetY = (((float)(seed >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float offsetZ = (((float)(seed >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        poseStack.translate(offsetX, offsetY, offsetZ);
        if (state.isNewRender) {
            newRender(state, poseStack);
        } else {
            oldRender(state, poseStack);
        }

        float hurt = state.hurtTime;
        if (hurt > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(hurt) * hurt * state.damageTime / 10.0F * state.hurtDir));
        }

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        submitNodeCollector.submitModel(this.model, state, poseStack, this.texture, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        poseStack.popPose();
    }

    private static void newRender(MinecartRenderState state, PoseStack poseStack) {
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-state.xRot));
        poseStack.translate(0.0F, 0.375F, 0.0F);
    }

    private static void oldRender(MinecartRenderState state, PoseStack poseStack) {
        double entityX = state.x;
        double entityY = state.y;
        double entityZ = state.z;
        float xRot = state.xRot;
        float rotation = state.yRot;
        if (state.posOnRail != null && state.frontPos != null && state.backPos != null) {
            Vec3 frontPos = state.frontPos;
            Vec3 backPos = state.backPos;
            poseStack.translate(state.posOnRail.x - entityX, (frontPos.y + backPos.y) / 2.0 - entityY, state.posOnRail.z - entityZ);
            Vec3 direction = backPos.add(-frontPos.x, -frontPos.y, -frontPos.z);
            if (direction.length() != 0.0) {
                direction = direction.normalize();
                rotation = (float)(Math.atan2(direction.z, direction.x) * 180.0 / Math.PI);
                xRot = (float)(Math.atan(direction.y) * 73.0);
            }
        }

        poseStack.translate(0.0F, 0.375F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - rotation));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-xRot));
    }

    @Override
    public void extractRenderState(AbstractMinecart entity, MinecartRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (entity.getBehavior() instanceof NewMinecartBehavior behavior) {
            newExtractState(entity, behavior, state, partialTicks);
            state.isNewRender = true;
        } else if (entity.getBehavior() instanceof OldMinecartBehavior behavior) {
            oldExtractState(entity, behavior, state, partialTicks);
            state.isNewRender = false;
        }

        long seed = entity.getId() * 493286711L;
        state.offsetSeed = seed * seed * 4392167121L + seed * 98761L;
        state.hurtTime = entity.getHurtTime() - partialTicks;
        state.hurtDir = entity.getHurtDir();
        state.damageTime = Math.max(entity.getDamage() - partialTicks, 0.0F);
        state.displayOffset = entity.getDisplayOffset();
    }

    private static void newExtractState(AbstractMinecart entity, NewMinecartBehavior behavior, MinecartRenderState state, float partialTicks) {
        if (behavior.cartHasPosRotLerp()) {
            state.renderPos = behavior.getCartLerpPosition(partialTicks);
            state.xRot = behavior.getCartLerpXRot(partialTicks);
            state.yRot = behavior.getCartLerpYRot(partialTicks);
        } else {
            state.renderPos = null;
            state.xRot = entity.getXRot();
            state.yRot = entity.getYRot();
        }
    }

    private static void oldExtractState(AbstractMinecart entity, OldMinecartBehavior behavior, MinecartRenderState state, float partialTicks) {
        state.xRot = entity.getXRot(partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        double entityX = state.x;
        double entityY = state.y;
        double entityZ = state.z;
        Vec3 pos = behavior.getPos(entityX, entityY, entityZ);
        if (pos != null) {
            state.posOnRail = pos;
            Vec3 p0 = behavior.getPosOffs(entityX, entityY, entityZ, 0.3F);
            Vec3 p1 = behavior.getPosOffs(entityX, entityY, entityZ, -0.3F);
            state.frontPos = Objects.requireNonNullElse(p0, pos);
            state.backPos = Objects.requireNonNullElse(p1, pos);
        } else {
            state.posOnRail = null;
            state.frontPos = null;
            state.backPos = null;
        }
    }

    @Override
    public MinecartRenderState createRenderState() {
        return new MinecartRenderState();
    }

    @Override
    public Vec3 getRenderOffset(MinecartRenderState state) {
        Vec3 offset = super.getRenderOffset(state);
        return state.isNewRender && state.renderPos != null
                ? offset.add(state.renderPos.x - state.x, state.renderPos.y - state.y, state.renderPos.z - state.z)
                : offset;
    }
}
