package me.superckl.conduits.client;

import me.superckl.conduits.Conduits;
import me.superckl.conduits.ModBlocks;
import me.superckl.conduits.PartType;
import me.superckl.conduits.client.model.ConduitModel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD, modid = Conduits.MOD_ID)
public class ClientRegistration {

	@SubscribeEvent
	public void clientSetup(final FMLClientSetupEvent e) {
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.CONDUIT_BLOCK.get(), RenderType.cutout());
	}

	@SubscribeEvent
	public static void registerModelLoader(final ModelRegistryEvent e) {
		//Declare the loader for the conduit model
		ModelLoaderRegistry.registerLoader(ConduitModel.Loader.LOCATION, ConduitModel.Loader.INSTANCE);
	}

	@SubscribeEvent
	public static void textureStitch(final TextureStitchEvent.Pre e) {
		//We need to stitch the connected joint texture because it's not on any model face by default
		//(It's placed onto the joint by the model baker)
		e.addSprite(new ResourceLocation(Conduits.MOD_ID, PartType.JOINT.path(null)));
	}

}
