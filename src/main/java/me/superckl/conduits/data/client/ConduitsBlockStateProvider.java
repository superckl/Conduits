package me.superckl.conduits.data.client;

import java.util.Collection;
import java.util.Map;

import com.mojang.math.Vector3f;

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
import net.minecraftforge.client.model.generators.ModelBuilder.FaceRotation;
import net.minecraftforge.client.model.generators.ModelBuilder.Perspective;
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

		if(2 != ConduitType.values().length)
			throw new IllegalStateException("Number of types does not match number of segment models!");
		final Vector3f offset1 = new Vector3f(2F, 0, 0);
		final Vector3f offset2 = new Vector3f(-2F, 0, 0);
		//JOINTS
		//Faces are textured by the model loader
		//Joint for one type
		this.jointElement(blocks.getBuilder(PartType.JOINT.path(null)+"_1"), 1, Vector3f.ZERO);

		//Joints for two types
		this.jointElement(this.jointElement(blocks.getBuilder(PartType.JOINT.path(null)+"_2"), 1, offset1), 2, offset2);

		//SEGMENTS
		//Faces are textures by the model loader
		//Segments for one type
		this.segmentElement(blocks.getBuilder(PartType.SEGMENT.path(null)+"_1"), 1, Vector3f.ZERO);

		//Segments for two types
		this.segmentElement(this.segmentElement(blocks.getBuilder(PartType.SEGMENT.path(null)+"_2"),
				1, offset1), 2, offset2);

		//MIXED JOINT
		blocks.getBuilder(PartType.MIXED_JOINT.path(null)).element()
		.from(4F, 4F, 4F).to(12F, 12F, 12F).allFaces((dir, face) -> face.texture("#mixed_joint")).end();

		//MACHINE CONNECTION
		blocks.getBuilder(PartType.CONNECTION.path(null)).element()
		.from(3F, 15.75F, 3F).to(13F, 16F, 13F).allFaces((dir, builder) -> builder.texture("#connection")).end().element()
		.from(4.5F, 15.5F, 4.5F).to(11.5F, 15.75F, 11.5F)
		.face(Direction.NORTH).texture("#connection").end()
		.face(Direction.SOUTH).texture("#connection").end()
		.face(Direction.EAST).texture("#connection").end()
		.face(Direction.WEST).texture("#connection").end()
		.face(Direction.DOWN).texture("#connection").end();

		//CONDUIT BLOCK MODEL
		//defers to model loader and exposes textures
		final BlockModelBuilder conduitBuilder = blocks.getBuilder("conduit");
		conduitBuilder.parent(this.models().getExistingFile(this.mcLoc("block")));
		conduitBuilder.customLoader(ConduitModelLoaderBuilder::new);
		conduitBuilder.texture("particle", this.mcLoc("block/stone"));//TODO temp particle texture
		conduitBuilder.texture("mixed_joint", PartType.MIXED_JOINT.path(null));
		conduitBuilder.texture("connection", PartType.MIXED_JOINT.path(null));
		conduitBuilder.texture("connected", PartType.JOINT.path(null));
		for(final ConduitType type:ConduitType.values()) {
			conduitBuilder.texture("unconnected_"+type.getSerializedName(), PartType.JOINT.path(type));
			conduitBuilder.texture("segment_"+type.getSerializedName(), PartType.SEGMENT.path(type));
		}
		final BlockModelBuilder.TransformsBuilder transforms = conduitBuilder.transforms();
		transforms.transform(Perspective.GUI).rotation(30F, 225F, 0F).scale(0.75F).end();

		//CONDUIT BLOCKSTATE
		//point to conduit block model
		final VariantBlockStateBuilder builder = this.getVariantBuilder(ModBlocks.CONDUIT_BLOCK.get());
		builder.setModels(builder.partialState(), ConfiguredModel.builder().modelFile(conduitBuilder).build());

		//ITEMS
		ModItems.CONDUITS.values().stream().map(Map::values).flatMap(Collection::stream).map(RegistryObject::get).forEach(item ->
		this.itemModels().getBuilder(item.getRegistryName().getPath()).parent(conduitBuilder)
				);

	}

	private BlockModelBuilder segmentElement(final BlockModelBuilder builder, final int index, final Vector3f offset) {
		return builder.element().from(7F+offset.x(), 9.5F+offset.y(), 7F+offset.z()).to(9F+offset.x(), 16F+offset.y(), 9F+offset.z())
				.face(Direction.NORTH).texture("#segment_"+index).uvs(0, 0, 13, 4).rotation(FaceRotation.CLOCKWISE_90).end()//is UV necessary?
				.face(Direction.SOUTH).texture("#segment_"+index).uvs(0, 0, 13, 4).rotation(FaceRotation.CLOCKWISE_90).end()
				.face(Direction.EAST).texture("#segment_"+index).uvs(0, 0, 13, 4).rotation(FaceRotation.CLOCKWISE_90).end()
				.face(Direction.WEST).texture("#segment_"+index).uvs(0, 0, 13, 4).rotation(FaceRotation.CLOCKWISE_90).end()
				.face(Direction.UP).texture("#segment_"+index).uvs(0, 0, 2, 2).end().end();
	}

	private BlockModelBuilder jointElement(final BlockModelBuilder builder, final int index, final Vector3f offset) {
		return builder.element().from(6.5F+offset.x(), 6.5F+offset.y(), 6.5F+offset.z()).to(9.5F+offset.x(), 9.5F+offset.y(), 9.5F+offset.z())
				.allFaces((dir, face) -> face.texture("#joint_"+index).uvs(0, 0, 16, 16)).end();
	}

	public static class ConduitModelLoaderBuilder extends CustomLoaderBuilder<BlockModelBuilder>{

		public ConduitModelLoaderBuilder(final BlockModelBuilder parent, final ExistingFileHelper existingFileHelper) {
			super(new ResourceLocation(Conduits.MOD_ID, "conduit"), parent, existingFileHelper);
		}

	}

}
