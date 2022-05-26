package me.superckl.conduits.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;

public class MixedJointOverlay implements IIngameOverlay{

	//private final Minecraft mc = Minecraft.getInstance();

	@Override
	public void render(final ForgeIngameGui gui, final PoseStack poseStack, final float partialTick, final int width, final int height) {
		/*
		if(this.mc.hitResult instanceof BlockHitResult blockHit) {
			BlockPos blockPos = blockHit.getBlockPos();
			this.mc.level.getBlockEntity(blockPos, ModBlocks.CONDUIT_ENTITY.get()).ifPresent(conduit -> {
				Vec3 hit = blockHit.getLocation().subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
				conduit.getConnections().getParts().findPart(hit).filter(part -> part.type() == ConduitPartType.MIXED_JOINT)
				.ifPresent(x -> {
					conduit.getConnections().getTypes().stream()
					.map(type -> ModItems.CONDUITS.get(type).get(conduit.getTier(type)).get().getDefaultInstance())
					.forEach(stack -> {
						this.mc.getItemRenderer().renderAndDecorateItem(stack, width/2, height/2);
					});

				});
			});
		}*/
	}

}
