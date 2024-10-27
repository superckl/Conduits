package me.superckl.conduits.client.model;

import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import lombok.RequiredArgsConstructor;
import me.superckl.conduits.ModConduits;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.ConduitShapeHelper;
import me.superckl.conduits.conduit.ConduitShapeHelper.Boxf;
import me.superckl.conduits.conduit.ConduitTier;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.conduit.ConfiguredConduit;
import me.superckl.conduits.conduit.connection.ConduitConnectionMap;
import me.superckl.conduits.conduit.part.ConduitPart;
import me.superckl.conduits.conduit.part.ConduitParts;
import me.superckl.conduits.util.ConduitUtil;
import me.superckl.conduits.util.VectorHelper;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.IModelBuilder;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@RequiredArgsConstructor
public class ConduitBakedModel implements IDynamicBakedModel {

    private static final Lazy<ConduitConnectionMap> DEFAULT = Lazy.of(() ->
            Util.make(ConduitConnectionMap.make(),
                    data -> data.setTier(ModConduits.ENERGY.get(), ConduitTier.EARLY))
    );

    private final ConduitParts<? extends WrappedElementsModel> parts;
    private final IGeometryBakingContext owner;
    private final ModelBaker bakery;
    private final Function<Material, TextureAtlasSprite> spriteGetter;
    private final ModelState modelTransform;
    private final ItemOverrides overrides;

    private final Map<ConduitConnectionMap, List<BakedQuad>> modelCache = ConduitConnectionMap.newConduitCache(true);


    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
        if (side != null)
            return Collections.emptyList();
        ConduitConnectionMap data = ConduitBakedModel.DEFAULT.get();
        if (extraData.has(ConduitBlockEntity.CONNECTION_PROPERTY))
            data = extraData.get(ConduitBlockEntity.CONNECTION_PROPERTY);
        return ConduitUtil.copyComputeIfAbsent(this.modelCache, data, x -> this.bake(x).getQuads(state, side, rand, extraData, renderType));
    }

    private BakedModel bake(final ConduitConnectionMap data) {
        final TextureAtlasSprite particle = this.spriteGetter.apply(this.owner.getMaterial("#particle"));
        ResourceLocation renderTypeHint = this.owner.getRenderTypeHint();
        var renderTypes = renderTypeHint != null ? this.owner.getRenderType(renderTypeHint) : RenderTypeGroup.EMPTY;
        final IModelBuilder<?> builder = IModelBuilder.of(this.useAmbientOcclusion(), this.usesBlockLight(), this.isGui3d(), this.owner.getTransforms(), this.overrides, particle, renderTypes);

        final ConfiguredConduit parts = data.getParts();

        //Segments
        parts.segments().values().forEach(segment -> {
            final ConduitType<?> type = segment.conduitType();
            //Texture segments to their corresponding type
            final BiFunction<Direction, String, String> texturer = (x, y) -> "#segment_" + type.getResourceLocation().getPath();
            this.addQuads(segment, texturer, builder);
        });

        //Joints
        final BooleanObjectPair<Direction> jointState = data.jointState();
        if (jointState.leftBoolean())
            this.addQuads(parts.mixedJoint(), builder);
        else {
            final Direction passThrough = jointState.right();
            parts.joints().forEach(joint -> {
                final ConduitType<?> type = joint.conduitType();
                final BiFunction<Direction, String, String> texturer = (faceDir, text) -> {
                    //The joint model will be rotated to the passthrough direction
                    //so we retexture the "origin" faces, and only down if the passthrough is actually passing through
                    if (passThrough != null && (Direction.UP.equals(faceDir) || Direction.DOWN.equals(faceDir) && data.hasConnection(type, passThrough.getOpposite())))
                        return "#connected";
                    //Texture connected faces if they haven't been handled by passthrough
                    if (passThrough == null && data.hasConnection(type, faceDir))
                        return "#connected";
                    //Default unconnected texture
                    return "#unconnected_" + type.getResourceLocation().getPath();
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

    private WrappedElementsModel toModel(final ConduitPart part, final BiFunction<Direction, String, String> texturer) {
        final Vector3f offset = VectorHelper.copy(part.offset());
        offset.mul(16);
        WrappedElementsModel model = this.parts.get(part.type());
        if (part.shape() != null) {
            final Boxf size = ConduitShapeHelper.toModelBox(part.shape());
            model = model.size(size.lowerCorner(), size.upperCorner());
        }
        return model.offset(offset).retexture(texturer);
    }

    private void addQuads(final ConduitPart part, final BiFunction<Direction, String, String> texturer, final IModelBuilder<?> builder) {
        this.toModel(part, texturer).addQuads(this.owner, builder, this.bakery, this.spriteGetter, new ConduitModelTransform(this.modelTransform, part.rotation()));
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
        return this.owner.getTransforms();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.spriteGetter.apply(this.owner.getMaterial("#particle"));
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrides;
    }

}
