package cn.noryea.fastitems.mixin;

import cn.noryea.fastitems.config.FastItemsConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Mixin(ItemEntityRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class ItemEntityRendererMixin extends EntityRenderer<ItemEntity, ItemEntityRenderState> {

    @Final
    @Shadow
    private RandomSource random;

    protected ItemEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    public void submit(ItemEntityRenderState itemEntityRenderState, PoseStack poseStack,
                       SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState,
                       CallbackInfo ci) {
        if (!FastItemsConfig.enable) return;
        if (itemEntityRenderState.item.isEmpty()) return;

        poseStack.pushPose();

        this.shadowRadius = FastItemsConfig.castShadows ? 0.15F : 0.0F;

        AABB aABB = itemEntityRenderState.item.getModelBoundingBox();
        float f = -((float) aABB.minY) + 0.0625F;

        float g = Mth.sin((double)(itemEntityRenderState.ageInTicks / 10.0F
                + itemEntityRenderState.bobOffset)) * 0.1F + 0.1F;
        poseStack.translate(0.0F, g + f, 0.0F);

        // face to camera instead of spinning
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());

        ItemEntityRenderer.submitMultipleFromCount(poseStack, submitNodeCollector,
                itemEntityRenderState.lightCoords, itemEntityRenderState, this.random, aABB);

        poseStack.popPose();
        super.submit(itemEntityRenderState, poseStack, submitNodeCollector, cameraRenderState);
        ci.cancel();
    }
}