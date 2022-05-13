package me.superckl.conduits.conduit.part;

import java.util.List;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import lombok.Builder;
import me.superckl.conduits.conduit.ConduitShapeHelper;
import me.superckl.conduits.conduit.ConduitTier;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.util.Util;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@Builder
public record ConduitPart(ConduitPartType type, ConduitTier tier, ConduitType conduitType, Vector3f offset, Quaternion rotation) {

	public VoxelShape getShape() {
		final List<AABB> boxes = ConduitShapeHelper.getShape(this.type).toAabbs();
		final Vec3 offset = new Vec3(this.offset);
		return boxes.stream().map(box -> box.move(offset)).map(box -> Util.rotateModelAABB(box, this.rotation))
				.map(Shapes::create).reduce(Shapes::or).orElseGet(Shapes::empty);
	}

}
