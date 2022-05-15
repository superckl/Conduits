package me.superckl.conduits.client.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.mojang.math.Vector3f;

import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import lombok.RequiredArgsConstructor;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.ConduitShapeHelper;
import me.superckl.conduits.conduit.ConduitShapeHelper.Boxf;
import me.superckl.conduits.conduit.ConduitTier;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.conduit.ConfiguredConduit;
import me.superckl.conduits.conduit.connection.ConduitConnectionMap;
import me.superckl.conduits.conduit.part.ConduitPart;
import me.superckl.conduits.conduit.part.ConduitParts;
import net.minecraft.Util;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

@RequiredArgsConstructor
public class ConduitBakedModel implements BakedModel{

	private static final ConduitConnectionMap DEFAULT = Util.make(ConduitConnectionMap.make(),
			data -> data.setTier(ConduitType.ENERGY, ConduitTier.EARLY));

	private final ConduitParts<? extends WrappedVanillaProxy> parts;
	private final IModelConfiguration owner;
	private final ModelBakery bakery;
	private final Function<Material, TextureAtlasSprite> spriteGetter;
	private final ModelState modelTransform;
	private final ItemOverrides overrides;
	private final ResourceLocation modelLocation;

	private final Map<ConduitConnectionMap, List<BakedQuad>> modelCache = ConduitConnectionMap.newConduitCache(true);

	@Override
	public List<BakedQuad> getQuads(final BlockState pState, final Direction pSide, final Random pRand) {
		return this.getQuads(pState, pSide, pRand, EmptyModelData.INSTANCE);
	}

	@Override
	public List<BakedQuad> getQuads(final BlockState state, final Direction side, final Random rand, final IModelData extraData) {
		if(side != null)
			return Collections.emptyList();
		ConduitConnectionMap data = ConduitBakedModel.DEFAULT;
		if(extraData.hasProperty(ConduitBlockEntity.CONNECTION_PROPERTY))
			data = extraData.getData(ConduitBlockEntity.CONNECTION_PROPERTY);
		return this.modelCache.computeIfAbsent(data, x -> this.bake(x).getQuads(state, null, rand, extraData));
	}

	private BakedModel bake(final ConduitConnectionMap data) {
		final TextureAtlasSprite particle = this.spriteGetter.apply(this.owner.resolveTexture("particle"));
		final IModelBuilder<?> builder = IModelBuilder.of(this.owner, this.overrides, particle);

		final ConfiguredConduit parts = data.getParts();

		//Segments
		parts.segments().values().forEach(segment -> {
			final ConduitType type = segment.conduitType();
			//Texture segments to their corresponding type
			final BiFunction<Direction, String, String> texturer = (x, y) -> "#segment_"+type.getSerializedName();
			this.addQuads(segment, texturer, builder);
		});

		//Joints
		final BooleanObjectPair<Direction> jointState = data.jointState();
		if(jointState.leftBoolean())
			this.addQuads(parts.mixedJoint(), builder);
		else {
			final Direction passThrough = jointState.right();
			parts.joints().forEach(joint -> {
				final ConduitType type = joint.conduitType();
				final BiFunction<Direction, String, String> texturer = (faceDir, text) -> {
					//The joint model will be rotated to the passthrough direction
					//so we retexture the "origin" faces, and only down if the passthrough is actually passing through
					if(passThrough != null && (Direction.UP.equals(faceDir) || Direction.DOWN.equals(faceDir) && data.hasConnection(type, passThrough.getOpposite())))
						return "#connected";
					//Texture connected faces if they haven't been handled by passthrough
					if(passThrough == null && data.hasConnection(type, faceDir))
						return "#connected";
					//Default unconnected texture
					return "#unconnected_"+type.getSerializedName();
				};
				this.addQuads(joint, texturer, builder);
			});
		}

		//Connections
		parts.connections().values().forEach(part -> {
			this.addQuads(part, builder);
		});
		return builder.build();
	}

	private WrappedVanillaProxy toModel(final ConduitPart part, final BiFunction<Direction, String, String> texturer) {
		final Vector3f offset = part.offset().copy();
		offset.mul(16);
		WrappedVanillaProxy model = this.parts.get(part.type());
		if(part.shape() != null) {
			final Boxf size = ConduitShapeHelper.toModelBox(part.shape());
			model = model.size(size.lowerCorner(), size.upperCorner());
		}
		return model.offset(offset).retexture(texturer);
	}

	private void addQuads(final ConduitPart part, final BiFunction<Direction, String, String> texturer, final IModelBuilder<?> builder) {
		this.toModel(part, texturer).addQuads(this.owner, builder, this.bakery, this.spriteGetter, new ConduitModelTransform(this.modelTransform, part.rotation()), this.modelLocation);
	}

	private void addQuads(final ConduitPart part, final IModelBuilder<?> builder) {
		this.addQuads(part, (dir, texture) -> texture, builder);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Override
	public boolean usesBlockLight() {
		return true;
	}

	@Override
	public boolean isCustomRenderer() {
		return true;
	}

	@Override
	public ItemTransforms getTransforms() {
		return this.owner.getCameraTransforms();
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return this.spriteGetter.apply(this.owner.resolveTexture("#particle"));
	}

	@Override
	public ItemOverrides getOverrides() {
		return this.overrides;
	}

}
