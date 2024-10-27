package me.superckl.conduits.client;

import com.mojang.blaze3d.vertex.PoseStack;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class ConduitBlockEntityRenderer implements BlockEntityRenderer<ConduitBlockEntity> {

    @Override
    public void render(final ConduitBlockEntity pBlockEntity, final float pPartialTick, final PoseStack pPoseStack,
                       final MultiBufferSource pBufferSource, final int pPackedLight, final int pPackedOverlay) {
        // TODO Auto-generated method stub

    }

    public static class Provider implements BlockEntityRendererProvider<ConduitBlockEntity> {

        @Override
        public ConduitBlockEntityRenderer create(final Context pContext) {
            return new ConduitBlockEntityRenderer();
        }

    }

}
