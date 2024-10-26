package me.superckl.conduits.conduit.part;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import lombok.Builder;
import me.superckl.conduits.conduit.ConduitShapeHelper;
import me.superckl.conduits.conduit.ConduitTier;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.conduit.connection.ConduitConnectionMap;
import me.superckl.conduits.util.ConduitUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Builder
public record ConduitPart(ConduitPartType type, ConduitTier tier, ConduitType<?> conduitType,
						  Vector3f offset, AABB shape, Quaternionf rotation) {

	public static final Vector3f ONE = new Vector3f(1, 1, 1);

	//Global cache for part -> shape since the computation may be expensive for a complicated joint
	private static final Map<ConduitPart, VoxelShape> SHAPE_CACHE = ConduitConnectionMap.newConduitCache(true);

	public VoxelShape getShape() {
		return ConduitPart.SHAPE_CACHE.computeIfAbsent(this, part -> {
			final List<AABB> boxes = this.shape == null ? ConduitShapeHelper.getShape(part.type).toAabbs() : Lists.newArrayList(this.shape);
			final Vec3 offset = new Vec3(part.offset);
			return boxes.stream().map(box -> box.move(offset))
					.map(box -> ConduitUtil.rotateModelAABB(box, part.rotation))
					.map(Shapes::create).reduce(Shapes::or).orElseGet(Shapes::empty);
		});
	}

	public boolean contains(final Vec3 location) {
		return this.getShape().toAabbs().stream().anyMatch(box -> ConduitUtil.containsInclusive(box, location));
	}

}
