package me.superckl.conduits.client.model;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

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

public class JointModel implements ISimpleModelGeometry<JointModel>{

	private final BlockElement joint;
	//private final String unconnectedTexture;
	//private final String connectedTexture;
	private final VanillaProxy wrapped;

	public JointModel(final BlockElement el) {
		this.joint = el;
		//this.unconnectedTexture = unconnectedTexture;
		//this.connectedTexture = connectedTexture;
		this.wrapped = new VanillaProxy(Lists.newArrayList(el));
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
		//return Lists.newArrayList(owner.resolveTexture(this.unconnectedTexture), owner.resolveTexture(this.connectedTexture));
		return this.wrapped.getTextures(owner, modelGetter, missingTextureErrors);
	}

	public JointModel retexture(final Function<Direction, String> texturer) {
		final Map<Direction, BlockElementFace> faces = new EnumMap<>(Direction.class);
		this.joint.faces.forEach((dir, face) -> {
			faces.put(dir, new BlockElementFace(face.cullForDirection, face.tintIndex, texturer.apply(dir), face.uv));
		});
		return new JointModel(new BlockElement(this.joint.from, this.joint.to, faces, this.joint.rotation, this.joint.shade));
	}

	public static class Loader implements IModelLoader<JointModel>{

		public static final Loader INSTANCE = new Loader();

		private Loader() {}

		@Override
		public void onResourceManagerReload(final ResourceManager pResourceManager) {}

		@Override
		public JointModel read(final JsonDeserializationContext context, final JsonObject obj) {
			//final JsonObject textures = obj.getAsJsonObject("textures");
			return new JointModel(this.getModelElement(context, obj));
		}

		private BlockElement getModelElement(final JsonDeserializationContext deserializationContext, final JsonObject object) {
			if (object.has("elements")) {
				final JsonArray array = GsonHelper.getAsJsonArray(object, "elements");
				if(array.size() != 1)
					throw new IllegalArgumentException("Joint model requires exactly one block element!");
				return deserializationContext.deserialize(array.get(0), BlockElement.class);
			}
			return null;
		}

	}

}
