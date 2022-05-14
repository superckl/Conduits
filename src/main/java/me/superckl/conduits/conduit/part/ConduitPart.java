package me.superckl.conduits.conduit.part;

import java.util.List;
import java.util.Map;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Builder;
import me.superckl.conduits.conduit.ConduitShapeHelper;
import me.superckl.conduits.conduit.ConduitTier;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.conduit.connection.ConduitConnectionMap;
import me.superckl.conduits.util.Util;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@Builder
public record ConduitPart(ConduitPartType type, ConduitTier tier, ConduitType conduitType, Vector3f offset, Quaternion rotation) {

	//Global cache for part -> shape since the computation may be expensive for a complicated joint
	private static final Map<ConduitPart, VoxelShape> SHAPE_CACHE = new Object2ObjectOpenHashMap<>(ConduitConnectionMap.states());

	public VoxelShape getShape() {
		return ConduitPart.SHAPE_CACHE.computeIfAbsent(this, part -> {
			final List<AABB> boxes = ConduitShapeHelper.getShape(part.type).toAabbs();
			final Vec3 offset = new Vec3(part.offset);
			return boxes.stream().map(box -> box.move(offset)).map(box -> Util.rotateModelAABB(box, part.rotation))
					.map(Shapes::create).reduce(Shapes::or).orElseGet(Shapes::empty);
		});
	}

	public boolean contains(final Vec3 location) {
		return this.getShape().toAabbs().stream().anyMatch(box -> Util.containsInclusive(box, location));
	}

}
