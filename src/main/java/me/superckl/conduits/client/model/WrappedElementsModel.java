package me.superckl.conduits.client.model;

import me.superckl.conduits.util.VectorHelper;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.ElementsModel;
import net.neoforged.neoforge.client.model.IModelBuilder;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WrappedElementsModel extends ElementsModel {

    private final List<BlockElement> elements;

    public WrappedElementsModel(final List<BlockElement> elements) {
        super(elements);
        this.elements = elements;
    }

    @Override
    public void addQuads(final IGeometryBakingContext owner, final IModelBuilder<?> modelBuilder, final ModelBaker baker,
                         final Function<Material, TextureAtlasSprite> spriteGetter, final ModelState modelTransform) {
        super.addQuads(owner, modelBuilder, baker, spriteGetter, modelTransform);
    }

    public WrappedElementsModel retexture(final BiFunction<Direction, String, String> texturer) {
        final List<BlockElement> elements = this.elements.stream().map(el -> {
            final Map<Direction, BlockElementFace> faces = new EnumMap<>(Direction.class);
            el.faces.forEach((dir, face) -> {
                faces.put(dir, new BlockElementFace(face.cullForDirection(), face.tintIndex(), texturer.apply(dir, face.texture()), face.uv()));
            });
            return new BlockElement(el.from, el.to, faces, el.rotation, el.shade);
        }).collect(Collectors.toList());

        return new WrappedElementsModel(elements);
    }

    public WrappedElementsModel offset(final Vector3f offset) {
        final List<BlockElement> elements = this.elements.stream().map(el -> {
            final Vector3f from = VectorHelper.copy(el.from);
            final Vector3f to = VectorHelper.copy(el.to);
            from.add(offset);
            to.add(offset);
            return new BlockElement(from, to, el.faces, el.rotation, el.shade);
        }).collect(Collectors.toList());
        return new WrappedElementsModel(elements);
    }

    public WrappedElementsModel size(final Vector3f from, final Vector3f to) {
        final List<BlockElement> elements = this.elements.stream().map(el -> new BlockElement(from, to, el.faces, el.rotation, el.shade)).collect(Collectors.toList());
        return new WrappedElementsModel(elements);
    }

}
