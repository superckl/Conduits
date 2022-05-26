package me.superckl.conduits.client;

import me.superckl.conduits.Conduits;
import me.superckl.conduits.ModBlocks;
import me.superckl.conduits.ModConduits;
import me.superckl.conduits.ModContainers;
import me.superckl.conduits.client.model.ConduitModel;
import me.superckl.conduits.client.screen.InventoryConnectionScreen;
import me.superckl.conduits.conduit.part.ConduitPartType;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD, modid = Conduits.MOD_ID)
public class ClientRegistration {

	@SubscribeEvent
	public static void clientSetup(final FMLClientSetupEvent e) {
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.CONDUIT_BLOCK.get(), RenderType.cutout());
		OverlayRegistry.registerOverlayTop("conduit_mixed_joint", new MixedJointOverlay());
		MenuScreens.register(ModContainers.INVENTORY_CONNECTION.get(), InventoryConnectionScreen::new);
	}

	@SubscribeEvent
	public static void registerModelLoader(final ModelRegistryEvent e) {
		//Declare the loader for the conduit model
		ModelLoaderRegistry.registerLoader(ConduitModel.Loader.LOCATION, ConduitModel.Loader.INSTANCE);
	}

	@SubscribeEvent
	public static void textureStitch(final TextureStitchEvent.Pre e) {
		//We need to stitch the joint and segment textures because they're not on any model face by default
		//(They're placed onto the joints/segments by the baked model)
		ModConduits.TYPES.getEntries().forEach(obj -> {
			e.addSprite(new ResourceLocation(Conduits.MOD_ID, ConduitPartType.JOINT.path(obj)));
			e.addSprite(new ResourceLocation(Conduits.MOD_ID, ConduitPartType.SEGMENT.path(obj)));
		});
		e.addSprite(new ResourceLocation(Conduits.MOD_ID, ConduitPartType.JOINT.path(null)));
	}

}
