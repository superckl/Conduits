package me.superckl.conduits.data.client;

import java.util.Collection;
import java.util.Map;

import me.superckl.conduits.Conduits;
import me.superckl.conduits.ModBlocks;
import me.superckl.conduits.ModConduits;
import me.superckl.conduits.ModItems;
import me.superckl.conduits.conduit.ConduitShapeHelper;
import me.superckl.conduits.conduit.ConduitShapeHelper.Boxf;
import me.superckl.conduits.conduit.part.ConduitPartType;
import me.superckl.conduits.util.VectorHelper;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.generators.*;
import net.neoforged.neoforge.client.model.renderable.CompositeRenderable;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.joml.Vector3f;

public class ConduitsBlockStateProvider extends BlockStateProvider {

	public ConduitsBlockStateProvider(final DataGenerator gen, final ExistingFileHelper exFileHelper) {
		super(gen.getPackOutput(), Conduits.MOD_ID, exFileHelper);
	}

	@Override
	protected void registerStatesAndModels() {

		final BlockModelProvider blocks = this.models();

		//JOINTS
		//Faces are textured by the model loader
		//Joint for one type
		this.jointElement(blocks.getBuilder(ConduitPartType.JOINT.path(null)), 1, VectorHelper.ZERO);

		//Joints for two types
		//this.jointElement(this.jointElement(blocks.getBuilder(PartType.JOINT.path(null)+"_2"), 1, segmentOffsets2[0]), 2, segmentOffsets2[1]);

		//SEGMENTS
		//Faces are textures by the model loader
		//Segments for one type
		this.segmentElement(blocks.getBuilder(ConduitPartType.SEGMENT.path(null)), 1, VectorHelper.ZERO);

		//Segments for two types
		//this.segmentElement(this.segmentElement(blocks.getBuilder(PartType.SEGMENT.path(null)+"_2"),
		//		1, segmentOffsets2[0]), 2, segmentOffsets2[1]);

		//MIXED JOINT
		this.sizedElement(blocks.getBuilder(ConduitPartType.MIXED_JOINT.path(null)), ConduitShapeHelper.modelBox(ConduitPartType.MIXED_JOINT), VectorHelper.ZERO)
		.allFaces((dir, face) -> face.texture("#mixed_joint")).end();

		//MACHINE CONNECTION
		this.sizedElement(this.sizedElement(blocks.getBuilder(ConduitPartType.CONNECTION.path(null)), ConduitShapeHelper.connectionBottomModelBox(), VectorHelper.ZERO)
				.allFaces((dir, builder) -> builder.texture("#connection")).end(), ConduitShapeHelper.connectionTopModelBox(), VectorHelper.ZERO)
		.face(Direction.NORTH).texture("#connection").end()
		.face(Direction.SOUTH).texture("#connection").end()
		.face(Direction.EAST).texture("#connection").end()
		.face(Direction.WEST).texture("#connection").end()
		.face(Direction.DOWN).texture("#connection").end();

		//CONDUIT BLOCK MODEL
		//defers to model loader and exposes textures
		final BlockModelBuilder conduitBuilder = blocks.getBuilder("conduit");
		conduitBuilder.renderType("cutout");
		conduitBuilder.parent(this.models().getExistingFile(this.mcLoc("block")));
		conduitBuilder.customLoader(ConduitModelLoaderBuilder::new);
		conduitBuilder.texture("particle", this.mcLoc("block/stone"));//TODO temp particle texture
		conduitBuilder.texture("mixed_joint", ConduitPartType.MIXED_JOINT.path(null));
		conduitBuilder.texture("connection", ConduitPartType.MIXED_JOINT.path(null));
		conduitBuilder.texture("connected", ConduitPartType.JOINT.path(null));
		ModConduits.TYPES.getEntries().forEach(obj -> {
			conduitBuilder.texture("unconnected_"+obj.getId().getPath(), ConduitPartType.JOINT.path(obj));
			conduitBuilder.texture("segment_"+obj.getId().getPath(), ConduitPartType.SEGMENT.path(obj));
		});
		final BlockModelBuilder.TransformsBuilder transforms = conduitBuilder.transforms();
		transforms.transform(ItemDisplayContext.GUI).rotation(30F, 225F, 0F).scale(0.75F).end();

		//CONDUIT BLOCKSTATE
		//point to conduit block model
		final VariantBlockStateBuilder builder = this.getVariantBuilder(ModBlocks.CONDUIT_BLOCK.get());
		builder.setModels(builder.partialState(), ConfiguredModel.builder().modelFile(conduitBuilder).build());

		//ITEMS
		ModItems.CONDUITS.values().stream().map(Map::values).flatMap(Collection::stream).forEach(item ->
			this.itemModels().getBuilder(item.getId().getPath()).parent(conduitBuilder)
				);

		this.itemModels().singleTexture(ModItems.WRENCH.getId().getPath(), this.mcLoc("item/generated"), "layer0", this.modLoc("item/wrench"));

	}

	private BlockModelBuilder segmentElement(final BlockModelBuilder builder, final int index, final Vector3f offset) {
		return this.sizedElement(builder, ConduitShapeHelper.modelBox(ConduitPartType.SEGMENT), offset)
				.face(Direction.NORTH).texture("#segment_"+index).uvs(0, 0, 13.25F, 4.25F).rotation(ModelBuilder.FaceRotation.CLOCKWISE_90).end()//is UV necessary?
				.face(Direction.SOUTH).texture("#segment_"+index).uvs(0, 0, 13.25F, 4.25F).rotation(ModelBuilder.FaceRotation.CLOCKWISE_90).end()
				.face(Direction.EAST).texture("#segment_"+index).uvs(0, 0, 13.25F, 4.25F).rotation(ModelBuilder.FaceRotation.CLOCKWISE_90).end()
				.face(Direction.WEST).texture("#segment_"+index).uvs(0, 0, 13.25F, 4.25F).rotation(ModelBuilder.FaceRotation.CLOCKWISE_90).end()
				.face(Direction.UP).texture("#segment_"+index).uvs(1, 0, 2.25F, 1.25F).end().end();
	}

	private BlockModelBuilder jointElement(final BlockModelBuilder builder, final int index, final Vector3f offset) {
		return this.sizedElement(builder, ConduitShapeHelper.modelBox(ConduitPartType.JOINT), offset)
				.allFaces((dir, face) -> face.texture("#joint_"+index).uvs(0, 0, 16, 16)).end();
	}

	private BlockModelBuilder.ElementBuilder sizedElement(final BlockModelBuilder builder, final Boxf size, final Vector3f offset) {
		return builder.element().from(size.minX()+offset.x(), size.minY()+offset.y(), size.minZ()+offset.z())
				.to(size.maxX()+offset.x(), size.maxY()+offset.y(), size.maxZ()+offset.z());
	}

	public static class ConduitModelLoaderBuilder extends CustomLoaderBuilder<BlockModelBuilder> {

		public ConduitModelLoaderBuilder(final BlockModelBuilder parent, final ExistingFileHelper existingFileHelper) {
			super(ResourceLocation.fromNamespaceAndPath(Conduits.MOD_ID, "conduit"), parent, existingFileHelper, false);
		}

	}

}
