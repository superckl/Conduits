package me.superckl.conduits.conduit;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Multimap;

import me.superckl.conduits.conduit.part.ConduitPart;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
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

	public Optional<ConduitPart> findPart(final Vec3 location) {
		if(this.mixedJoint != null && this.mixedJoint.contains(location))
			return Optional.of(this.mixedJoint);
		final Optional<ConduitPart> joint = this.findPart(this.joints, location);
		if(joint.isPresent())
			return joint;
		final Optional<ConduitPart> connection = this.findPart(this.connections.values(), location);
		if(connection.isPresent())
			return connection;
		return this.findPart(this.segments.values(), location);
	}

	private Optional<ConduitPart> findPart(final Collection<ConduitPart> parts, final Vec3 location){
		return parts.stream().filter(part -> part.contains(location)).findFirst();
	}

}
