package me.superckl.conduits.client.model;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import lombok.RequiredArgsConstructor;
import me.superckl.conduits.Conduits;
import me.superckl.conduits.conduit.part.ConduitParts;
import me.superckl.conduits.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

@RequiredArgsConstructor
public class ConduitModel implements IModelGeometry<ConduitModel>{

	private final ConduitParts<? extends WrappedVanillaProxy> parts;

	@Override
	public BakedModel bake(final IModelConfiguration owner, final ModelBakery bakery,
			final Function<Material, TextureAtlasSprite> spriteGetter, final ModelState modelTransform, final ItemOverrides overrides,
			final ResourceLocation modelLocation) {
		return new ConduitBakedModel(this.parts, owner, bakery, spriteGetter, modelTransform, overrides, modelLocation);
	}

	@Override
	public Collection<Material> getTextures(final IModelConfiguration owner,
			final Function<ResourceLocation, UnbakedModel> modelGetter, final Set<Pair<String, String>> missingTextureErrors) {
		return this.parts.all().stream().map(geom -> geom.getTextures(owner, modelGetter, missingTextureErrors)).flatMap(Collection::stream).collect(Collectors.toSet());
	}

	public static class Loader implements IModelLoader<ConduitModel>{

		public static final Loader INSTANCE = new Loader();
		public static final ResourceLocation LOCATION = new ResourceLocation(Conduits.MOD_ID, "conduit");

		private ResourceManager manager = Minecraft.getInstance().getResourceManager();

		private Loader() {}

		@Override
		public void onResourceManagerReload(final ResourceManager pResourceManager) {
			this.manager = pResourceManager;
		}

		@Override
		public ConduitModel read(final JsonDeserializationContext deserializationContext, final JsonObject modelContents) {
			final ConduitParts<WrappedVanillaProxy> parts = ConduitParts.from(path -> {
				final ResourceLocation loc = new ResourceLocation(Conduits.MOD_ID, "models/"+path+".json");
				try {
					final JsonObject obj = Util.toJson(this.manager.getResource(loc)).getAsJsonObject();
					return WrappedVanillaProxy.Loader.INSTANCE.read(deserializationContext, obj);
				} catch(final IOException e) {
					throw new IllegalStateException("Unable to load conduit model resource "+loc.toString(), e);
				}
			}, WrappedVanillaProxy.class);
			return new ConduitModel(parts);
		}

	}

}
