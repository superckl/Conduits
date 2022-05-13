package me.superckl.conduits.conduit;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;

import me.superckl.conduits.conduit.part.ConduitPart;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public record ConfiguredConduit(ConduitType[] types, List<ConduitPart> joints, ConduitPart mixedJoint,
		Map<Direction, ConduitPart> connections, Multimap<Direction, ConduitPart> segments) {

	public VoxelShape getShape() {
		VoxelShape shape = Shapes.or(this.toShape(this.joints),
				this.toShape(this.connections.values()), this.toShape(this.segments.values()));
		if(this.mixedJoint != null)
			shape = Shapes.or(shape, this.mixedJoint.getShape());
		return shape;
	}

	private VoxelShape toShape(final Collection<ConduitPart> parts) {
		return parts.stream().map(ConduitPart::getShape).reduce(Shapes::or).orElseGet(Shapes::empty);
	}

}
