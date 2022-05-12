package me.superckl.conduits.client.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import me.superckl.conduits.ConduitParts;
import me.superckl.conduits.ConduitTier;
import me.superckl.conduits.ConduitType;
import me.superckl.conduits.PartType;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.common.block.ConduitBlockEntity.ConnectionData;
import me.superckl.conduits.common.block.ConduitBlockEntity.ConnectionType;
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

	private static final ConnectionData DEFAULT = Util.make(ConnectionData.make(),
			data -> data.setTier(ConduitType.ENERGY, ConduitTier.EARLY));

	private final ConduitParts<? extends WrappedVanillaProxy> parts;
	private final IModelConfiguration owner;
	private final ModelBakery bakery;
	private final Function<Material, TextureAtlasSprite> spriteGetter;
	private final ModelState modelTransform;
	private final ItemOverrides overrides;
	private final ResourceLocation modelLocation;

	private final Map<ConnectionData, List<BakedQuad>> modelCache = new Object2ObjectOpenHashMap<>(ConnectionData.states());

	@Override
	public List<BakedQuad> getQuads(final BlockState pState, final Direction pSide, final Random pRand) {
		return this.getQuads(pState, pSide, pRand, EmptyModelData.INSTANCE);
	}

	@Override
	public List<BakedQuad> getQuads(final BlockState state, final Direction side, final Random rand, final IModelData extraData) {
		if(side != null)
			return Collections.emptyList();
		ConnectionData data = ConduitBakedModel.DEFAULT;
		if(extraData.hasProperty(ConduitBlockEntity.CONNECTION_PROPERTY))
			data = extraData.getData(ConduitBlockEntity.CONNECTION_PROPERTY);
		return this.modelCache.computeIfAbsent(data, x -> this.bake(x).getQuads(state, null, rand, extraData));
	}

	private BakedModel bake(final ConnectionData data) {
		final TextureAtlasSprite particle = this.spriteGetter.apply(this.owner.resolveTexture("particle"));
		final IModelBuilder<?> builder = IModelBuilder.of(this.owner, this.overrides, particle);

		boolean mixed = true;
		final Direction passThrough;
		final var byDir = data.byDirection();
		final long numTypes = data.types().count();

		//Determine what kind of joint we have
		if(numTypes <= 1 || byDir.size() == 0) {
			//This is an isolated conduit, not mixed and not passthrough
			mixed = false;
			passThrough = null;
		}else if(byDir.size() == 1) {
			//This is the the end of series of conduits
			final Direction dir = byDir.keySet().iterator().next();
			if(byDir.get(dir).size() == numTypes) {
				//All types present in this conduit have a connection the previous, can do passthrough
				mixed = false;
				passThrough = byDir.keySet().iterator().next();
			}else
				passThrough = null; //A type is missing a connection, fallback to mixed
		}else if(byDir.size() == 2) {
			//This could be a corner or a straight passthrough
			final Direction dir = byDir.keySet().iterator().next();
			if(byDir.get(dir).size() == numTypes && byDir.containsKey(dir.getOpposite()) && this.isPassthrough(byDir.get(dir), byDir.get(dir.getOpposite()))) {
				//This is a straight passthrough
				mixed = false;
				passThrough = dir;
			}else
				passThrough = null; //Either a type is missing a connection or this is a corner, fallback to mixed
		}else
			passThrough = null; //This is a complicated joint, fallback to mixed

		if(mixed)
			//mixed joint
			this.parts.mixedJoint().addQuads(this.owner, builder, this.bakery, this.spriteGetter, this.modelTransform, this.modelLocation);
		else {
			//unmixed joint, requires retexturing and rotation
			final long count = data.types().count();

			WrappedVanillaProxy joints = this.parts.joints()[(int) (count-1)];
			//Types array, sorted to have consistent texturing
			final ConduitType[] types = data.types().toArray(s -> new ConduitType[s]);
			Arrays.sort(types, ConduitType::compareTo);
			final BiFunction<Direction, String, String> texturer = (dir, text) -> {
				//The joint model will be rotated to the passthrough direction
				//so we retexture the "origin" faces, and only down if the passthrough is actually passing through
				if(passThrough != null && (Direction.UP.equals(dir) || Direction.DOWN.equals(dir) && byDir.containsKey(passThrough.getOpposite())))
					return "#connected";
				for(int i = 1; i <= types.length; i++)
					if(("#joint_"+i).equals(text)) {
						final ConduitType type = types[i-1];
						//Texture connected faces if they haven't been handled by passthrough
						if(passThrough == null && data.hasConnection(type, dir))
							return "#connected";
						//Default unconnected texture
						return "#unconnected_"+type.getSerializedName();
					}
				throw new IllegalStateException("Failed to retexture a face: "+text);
			};
			joints = joints.retexture(texturer);
			ModelState transform = this.modelTransform;
			if(passThrough != null && passThrough != Direction.UP)
				transform = new ConduitModelTransform(transform, passThrough, PartType.JOINT);
			joints.addQuads(this.owner, builder, this.bakery, this.spriteGetter, transform, this.modelLocation);

		}
		//segments, requires retexturing and rotation
		byDir.forEach((connDir, typeMap) -> {
			WrappedVanillaProxy segments = this.parts.segments()[typeMap.size()-1];
			final ConduitType[] types = typeMap.keySet().toArray(new ConduitType[typeMap.size()]);
			Arrays.sort(types, ConduitType::compareTo);
			final BiFunction<Direction, String, String> texturer = (faceDir, text) -> {
				for(int i = 1; i <= types.length; i++)
					if(("#segment_"+i).equals(text))
						return "#segment_"+types[i-1].getSerializedName();
				throw new IllegalStateException("Failed to retexture a face: "+text);
			};
			segments = segments.retexture(texturer);
			ModelState transform = this.modelTransform;
			if(connDir != Direction.UP)
				transform = new ConduitModelTransform(transform, connDir, PartType.SEGMENT);
			segments.addQuads(this.owner, builder, this.bakery, this.spriteGetter, transform, this.modelLocation);
			if(typeMap.values().stream().map(Pair::getRight).anyMatch(ConnectionType.INVENTORY::equals))
				this.parts.inventoryConnection().addQuads(this.owner, builder, this.bakery, this.spriteGetter, transform, this.modelLocation);
		});
		return builder.build();
	}

	private boolean isPassthrough(final Map<ConduitType, Pair<ConduitTier, ConnectionType>> first,
			final Map<ConduitType, Pair<ConduitTier, ConnectionType>> second) {
		return first.keySet().equals(second.keySet());
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
