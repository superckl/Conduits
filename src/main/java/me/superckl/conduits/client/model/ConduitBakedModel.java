package me.superckl.conduits.client.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import me.superckl.conduits.ConduitParts;
import me.superckl.conduits.ConduitTier;
import me.superckl.conduits.ConduitType;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.common.block.ConduitBlockEntity.ConnectionData;
import me.superckl.conduits.common.block.ConduitBlockEntity.ConnectionType;
import net.minecraft.Util;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
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
import net.minecraftforge.client.model.geometry.ISimpleModelGeometry;

@RequiredArgsConstructor
public class ConduitBakedModel implements BakedModel{

	private static final ConnectionData DEFAULT = Util.make(ConnectionData.make(),
			data -> data.setTier(ConduitType.ENERGY, ConduitTier.EARLY));

	private final ConduitParts<? extends ISimpleModelGeometry<?>> parts;
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

		final boolean mixed = data.numTypes() > 1;
		//mixed joint
		if(mixed)
			this.parts.mixedJoint().addQuads(this.owner, builder, this.bakery, this.spriteGetter, this.modelTransform, this.modelLocation);
		data.types().forEach(type -> {
			//ConduitTier tier = data.getTier(type);
			final Map<Direction, ConnectionType> connections = data.getConnections(type);

			//unmixed joint
			if(!mixed) {
				final Map<Direction, String> texturer = new EnumMap<>(Direction.class);
				for(final Direction dir:Direction.values())
					texturer.put(dir, connections.containsKey(dir) ? "#connected" : "#unconnected_"+type.getSerializedName());
				((JointModel) this.parts.joints().get(type)).retexture(texturer::get).addQuads(this.owner, builder, this.bakery, this.spriteGetter, this.modelTransform, this.modelLocation);
			}

			//segments
			final ISimpleModelGeometry<?> segment = this.parts.segments().get(type);
			final Direction origin = Direction.UP;

			connections.forEach((dir, connectionType) -> {
				ModelState transform = this.modelTransform;
				if(dir != origin)
					transform = new ConduitModelTransform(transform, dir);
				segment.addQuads(this.owner, builder, this.bakery, this.spriteGetter, transform, this.modelLocation);
				if(connectionType == ConnectionType.INVENTORY)
					this.parts.inventoryConnection().addQuads(this.owner, builder, this.bakery, this.spriteGetter, transform, this.modelLocation);
			});
		});
		return builder.build();
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
	public TextureAtlasSprite getParticleIcon() {
		return this.spriteGetter.apply(this.owner.resolveTexture("#particle"));
	}

	@Override
	public ItemOverrides getOverrides() {
		return this.overrides;
	}

}
