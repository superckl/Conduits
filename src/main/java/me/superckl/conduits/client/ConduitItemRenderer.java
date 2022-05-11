package me.superckl.conduits.client;

import com.mojang.blaze3d.vertex.PoseStack;

import me.superckl.conduits.ModBlocks;
import me.superckl.conduits.common.item.ConduitItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;

public class ConduitItemRenderer extends BlockEntityWithoutLevelRenderer{

	public static final ConduitItemRenderer INSTANCE = new ConduitItemRenderer();
	public final ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();

	public ConduitItemRenderer() {
		super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
	}

	@Override
	public void renderByItem(final ItemStack pStack, final TransformType pTransformType, final PoseStack stack,
			final MultiBufferSource pBuffer, final int pPackedLight, final int pPackedOverlay) {
		if(pStack.getItem() instanceof final ConduitItem conduit) {

			stack.popPose();

			BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(ModBlocks.CONDUIT_BLOCK.get().defaultBlockState());
			//model = ForgeHooksClient.handleCameraTransforms(pPoseStack, model, pTransformType, false);
			model = ForgeHooksClient.handleCameraTransforms(stack, model, pTransformType, false);


			Minecraft.getInstance().getBlockRenderer().renderSingleBlock(ModBlocks.CONDUIT_BLOCK.get().defaultBlockState(),
					stack, pBuffer, pPackedLight, pPackedOverlay, conduit.getRenderData());

			stack.pushPose();
			//Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(pPoseStack.last(),
			//pBuffer.getBuffer(RenderType.cutout()), null, model, 1F, 1F, 1F, pPackedLight, pPackedOverlay, conduit.getRenderData());
			//pPoseStack.popPose();
		}
	}

}
