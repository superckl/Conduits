package me.superckl.conduits.client.model;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry.VanillaProxy;
import net.minecraftforge.client.model.geometry.ISimpleModelGeometry;

public class WrappedVanillaProxy implements ISimpleModelGeometry<WrappedVanillaProxy>{

	private final List<BlockElement> elements;
	private final VanillaProxy wrapped;

	public WrappedVanillaProxy(final List<BlockElement> elements) {
		this.elements = elements;
		this.wrapped = new VanillaProxy(elements);
	}

	@Override
	public void addQuads(final IModelConfiguration owner, final IModelBuilder<?> modelBuilder, final ModelBakery bakery,
			final Function<Material, TextureAtlasSprite> spriteGetter, final ModelState modelTransform,
			final ResourceLocation modelLocation) {
		this.wrapped.addQuads(owner, modelBuilder, bakery, spriteGetter, modelTransform, modelLocation);
	}

	@Override
	public Collection<Material> getTextures(final IModelConfiguration owner,
			final Function<ResourceLocation, UnbakedModel> modelGetter, final Set<Pair<String, String>> missingTextureErrors) {
		return this.wrapped.getTextures(owner, modelGetter, missingTextureErrors);
	}

	public WrappedVanillaProxy retexture(final BiFunction<Direction, String, String> texturer) {
		final List<BlockElement> elements = this.elements.stream().map(el -> {
			final Map<Direction, BlockElementFace> faces = new EnumMap<>(Direction.class);
			el.faces.forEach((dir, face) -> {
				faces.put(dir, new BlockElementFace(face.cullForDirection, face.tintIndex, texturer.apply(dir, face.texture), face.uv));
			});
			return new BlockElement(el.from, el.to, faces, el.rotation, el.shade);
		}).collect(Collectors.toList());

		return new WrappedVanillaProxy(elements);
	}

	public WrappedVanillaProxy offset(final Vector3f offset) {
		final List<BlockElement> elements = this.elements.stream().map(el -> {
			final Vector3f from = el.from.copy();
			final Vector3f to = el.to.copy();
			from.add(offset);
			to.add(offset);
			return new BlockElement(from, to, el.faces, el.rotation, el.shade);
		}).collect(Collectors.toList());
		return new WrappedVanillaProxy(elements);
	}

	public static class Loader implements IModelLoader<WrappedVanillaProxy>{

		public static final Loader INSTANCE = new Loader();

		private Loader() {}

		@Override
		public void onResourceManagerReload(final ResourceManager pResourceManager) {}

		@Override
		public WrappedVanillaProxy read(final JsonDeserializationContext context, final JsonObject obj) {
			return new WrappedVanillaProxy(this.getModelElements(context, obj));
		}

		private List<BlockElement> getModelElements(final JsonDeserializationContext deserializationContext, final JsonObject object) {
			final List<BlockElement> list = Lists.newArrayList();
			if (object.has("elements"))
				for(final JsonElement jsonelement : GsonHelper.getAsJsonArray(object, "elements"))
					list.add(deserializationContext.deserialize(jsonelement, BlockElement.class));

			return list;
		}

	}

}
