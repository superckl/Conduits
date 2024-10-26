package me.superckl.conduits.client.model;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import me.superckl.conduits.Conduits;
import me.superckl.conduits.conduit.part.ConduitParts;
import me.superckl.conduits.util.ConduitUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class ConduitUnbakedModel implements IUnbakedGeometry<ConduitUnbakedModel> {

	private final ConduitParts<? extends WrappedElementsModel> parts;

	@Override
	public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
		return new ConduitBakedModel(this.parts, context, baker, spriteGetter, modelState, overrides);
	}
	/*
	@Override
	public Collection<Material> getTextures(final IGeometryBakingContext owner,
			final Function<ResourceLocation, UnbakedModel> modelGetter, final Set<Pair<String, String>> missingTextureErrors) {
		return this.parts.all().stream().map(geom -> geom.getTextures(owner, modelGetter, missingTextureErrors)).flatMap(Collection::stream).collect(Collectors.toSet());
	}*/

	public static class Loader implements IGeometryLoader<ConduitUnbakedModel>, PreparableReloadListener {

		public static final Loader INSTANCE = new Loader();
		public static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(Conduits.MOD_ID, "conduit");

		private ResourceManager manager = Minecraft.getInstance().getResourceManager();

		private Loader() {}

		@Override
		public @NotNull ConduitUnbakedModel read(final JsonObject modelContents, final JsonDeserializationContext deserializationContext) {
			final ConduitParts<WrappedElementsModel> parts = ConduitParts.from(path -> {
				final ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(Conduits.MOD_ID, "models/"+path+".json");
                final JsonObject obj;
                try {
                    obj = ConduitUtil.toJson(this.manager.getResource(loc).orElseThrow()).getAsJsonObject();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read model data at "+loc);
                }
                return new WrappedElementsModel(getModelElements(deserializationContext, obj));
			}, WrappedElementsModel.class);
			return new ConduitUnbakedModel(parts);
		}

		private List<BlockElement> getModelElements(final JsonDeserializationContext deserializationContext, final JsonObject object) {
			final List<BlockElement> list = Lists.newArrayList();
			if (object.has("elements"))
				for(final JsonElement jsonelement : GsonHelper.getAsJsonArray(object, "elements"))
					list.add(deserializationContext.deserialize(jsonelement, BlockElement.class));

			return list;
		}

		@Override
		public @NotNull CompletableFuture<Void> reload(PreparationBarrier pPreparationBarrier, ResourceManager pResourceManager, ProfilerFiller pPreparationsProfiler, ProfilerFiller pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor) {
			this.manager = pResourceManager;
			return pPreparationBarrier.wait(null);
		}
	}

}
