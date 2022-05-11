package me.superckl.conduits.data.client;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import me.superckl.conduits.ConduitParts;
import me.superckl.conduits.ConduitType;
import me.superckl.conduits.Conduits;
import me.superckl.conduits.ModBlocks;
import me.superckl.conduits.ModItems;
import me.superckl.conduits.PartType;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder.Perspective;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ConduitsBlockStateProvider extends BlockStateProvider{

	public ConduitsBlockStateProvider(final DataGenerator gen, final ExistingFileHelper exFileHelper) {
		super(gen, Conduits.MOD_ID, exFileHelper);
	}

	@Override
	protected void registerStatesAndModels() {

		final BlockModelProvider blocks = this.models();

		//JOINTS
		//Faces are textured by the model loader, so we expose the textures for the loader
		final Map<ConduitType, BlockModelBuilder> jointModels = new EnumMap<>(ConduitType.class);
		for(final ConduitType type:ConduitType.values()) {
			final String texture = "#unconnected_"+type.getSerializedName();
			jointModels.put(type, blocks.getBuilder(PartType.JOINT.path(type))
					.element().from(6.5F, 6.5F, 6.5F).to(9.5F, 9.5F, 9.5F)
					.allFaces((dir, face) -> face.texture(texture).uvs(0, 0, 16, 16)).end());
		}

		//SEGMENTS
		final Map<ConduitType, BlockModelBuilder> segmentModels = new EnumMap<>(ConduitType.class);
		for(final ConduitType type:ConduitType.values()) {
			final String texture = "#segment_"+type.getSerializedName();
			segmentModels.put(type, blocks.getBuilder(PartType.SEGMENT.path(type))
					.element().from(7F, 9.5F, 7F).to(9F, 16F, 9F)
					.face(Direction.NORTH).texture(texture).uvs(0, 0, 13, 4).end()//is UV necessary?
					.face(Direction.SOUTH).texture(texture).uvs(0, 0, 13, 4).end()
					.face(Direction.EAST).texture(texture).uvs(0, 0, 13, 4).end()
					.face(Direction.WEST).texture(texture).uvs(0, 0, 13, 4).end().end());
		}

		//MIXED JOINT
		blocks.getBuilder(PartType.MIXED_JOINT.path(null)).element()
		.from(5F, 5F, 5F).to(11F, 11F, 11F).allFaces((dir, face) -> face.texture("#mixed_joint")).end();

		//MACHINE CONNECTION
		blocks.getBuilder(PartType.CONNECTION.path(null)).element()
		.from(5F, 15.75F, 5F).to(11F, 16F, 11F).allFaces((dir, builder) -> builder.texture("#connection")).end().element()
		.from(6.5F, 15.5F, 6.5F).to(9.5F, 15.75F, 9.5F)
		.face(Direction.NORTH).texture("#connection").end()
		.face(Direction.SOUTH).texture("#connection").end()
		.face(Direction.EAST).texture("#connection").end()
		.face(Direction.WEST).texture("#connection").end()
		.face(Direction.DOWN).texture("#connection").end();

		//CONDUIT BLOCK MODEL
		//defers to model loader
		final BlockModelBuilder conduitBuilder = blocks.getBuilder("conduit");
		conduitBuilder.parent(this.models().getExistingFile(this.mcLoc("block")));
		conduitBuilder.customLoader((builder, helper) -> new ConduitModelLoaderBuilder(builder, helper, ConduitParts.from((part, type) -> blocks.getExistingFile(this.modLoc(part.path(type))))));
		conduitBuilder.texture("particle", this.mcLoc("block/stone"));//TODO temp particle texture
		conduitBuilder.texture("mixed_joint", PartType.MIXED_JOINT.path(null));
		conduitBuilder.texture("connection", PartType.MIXED_JOINT.path(null));
		conduitBuilder.texture("connected", PartType.JOINT.path(null));
		for(final ConduitType type:ConduitType.values()) {
			conduitBuilder.texture("unconnected_"+type.getSerializedName(), PartType.JOINT.path(type));
			conduitBuilder.texture("segment_"+type.getSerializedName(), PartType.SEGMENT.path(type));
		}
		final BlockModelBuilder.TransformsBuilder transforms = conduitBuilder.transforms();
		transforms.transform(Perspective.GUI).rotation(30F, 225F, 0).scale(1F).end();

		//CONDUIT BLOCKSTATE
		//point to conduit block model
		final VariantBlockStateBuilder builder = this.getVariantBuilder(ModBlocks.CONDUIT_BLOCK.get());
		builder.setModels(builder.partialState(), ConfiguredModel.builder().modelFile(conduitBuilder).build());

		//ITEMS
		ModItems.CONDUITS.values().stream().map(Map::values).flatMap(Collection::stream).map(RegistryObject::get).forEach(item ->
		this.itemModels().getBuilder(item.getRegistryName().getPath()).parent(conduitBuilder)
				);

	}

	public static class ConduitModelLoaderBuilder extends CustomLoaderBuilder<BlockModelBuilder>{

		private final ConduitParts<ModelFile> parts;

		public ConduitModelLoaderBuilder(final BlockModelBuilder parent, final ExistingFileHelper existingFileHelper, final ConduitParts<ModelFile> parts) {
			super(new ResourceLocation(Conduits.MOD_ID, "conduit"), parent, existingFileHelper);
			this.parts = parts;
		}

		@Override
		public JsonObject toJson(final JsonObject json) {
			super.toJson(json);
			json.add("conduit_parts", this.parts.toJson(model -> new JsonPrimitive(model.getLocation().toString())));
			return json;
		}

	}

}
