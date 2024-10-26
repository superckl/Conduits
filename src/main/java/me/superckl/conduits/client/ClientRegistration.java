package me.superckl.conduits.client;

import me.superckl.conduits.*;
import me.superckl.conduits.client.model.ConduitUnbakedModel;
import me.superckl.conduits.conduit.part.ConduitPartType;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = Conduits.MOD_ID)
public class ClientRegistration {

	@SubscribeEvent
	public static void clientSetup(final FMLClientSetupEvent e) {
		//ItemBlockRenderTypes.setRenderLayer(ModBlocks.CONDUIT_BLOCK.get(), RenderType.cutout());
		//OverlayRegistry.registerOverlayTop("conduit_mixed_joint", new MixedJointOverlay());
		//MenuScreens.register(ModContainers.INVENTORY_CONNECTION.get(), InventoryConnectionScreen::new);
	}

	@SubscribeEvent
	public static void registerModelLoader(final ModelEvent.RegisterGeometryLoaders e) {
		//Declare the loader for the conduit model
		e.register(ConduitUnbakedModel.Loader.LOCATION, ConduitUnbakedModel.Loader.INSTANCE);
	}

	/*
	@SubscribeEvent
	public static void textureStitch(final TextureStitchEvent.Pre e) {
		//We need to stitch the joint and segment textures because they're not on any model face by default
		//(They're placed onto the joints/segments by the baked model)
		ModConduits.TYPES.getEntries().forEach(obj -> {
			e.addSprite(new ResourceLocation(Conduits.MOD_ID, ConduitPartType.JOINT.path(obj)));
			e.addSprite(new ResourceLocation(Conduits.MOD_ID, ConduitPartType.SEGMENT.path(obj)));
		});
		e.addSprite(new ResourceLocation(Conduits.MOD_ID, ConduitPartType.JOINT.path(null)));
	}*/

	@SubscribeEvent
	public static void registerClientExtensions(RegisterClientExtensionsEvent e){
		IClientItemExtensions conduit_render_extension = new IClientItemExtensions(){

			@Override
			public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return ConduitItemRenderer.INSTANCE;
			}
		};
		ModItems.CONDUITS.values().forEach(m -> m.values().forEach(h -> e.registerItem(conduit_render_extension, h)));
	}

	@SubscribeEvent
	public static void registerReloadListeners(RegisterClientReloadListenersEvent e){
		e.registerReloadListener(ConduitUnbakedModel.Loader.INSTANCE);
	}

	@SubscribeEvent
	public static void registerSpriteSourceType(RegisterSpriteSourceTypesEvent e){
		e.register(ResourceLocation.fromNamespaceAndPath(Conduits.MOD_ID, "directory"), ConduitDirectoryListerSource.TYPE);
	}

}
