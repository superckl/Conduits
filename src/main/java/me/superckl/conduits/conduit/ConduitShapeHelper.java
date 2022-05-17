package me.superckl.conduits.conduit;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import it.unimi.dsi.fastutil.floats.FloatFloatPair;
import me.superckl.conduits.conduit.connection.ConduitConnection;
import me.superckl.conduits.conduit.part.ConduitPartType;
import me.superckl.conduits.util.ConduitUtil;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ConduitShapeHelper {

	private static final VoxelShape SEGMENT = Shapes.box(7/16D, 9.5/16D, 7/16D, 9/16D, 16/16D, 9/16D);
	private static final VoxelShape JOINT = Shapes.box(6.5/16D, 6.5/16D, 6.5/16D, 9.5/16D, 9.5/16D, 9.5/16D);
	private static final VoxelShape MIXED_JOINT = Shapes.box(4.5/16D, 4.5/16D, 4.5/16D, 11.5/16D, 11.5/16D, 11.5/16D);
	private static final VoxelShape CONNECTION_BOTTOM = Shapes.box(3/16D, 15.75/16D, 3/16D, 13/16D, 16/16D, 13/16D);
	private static final VoxelShape CONNECTION_TOP = Shapes.box(4.5/16D, 15.5/16D, 4.5/16D, 11.5/16D, 15.75/16D, 11.5/16D);

	private static final Map<ConduitPartType, VoxelShape> BASE_SHAPES = Util.make(new EnumMap<>(ConduitPartType.class), map -> {
		map.put(ConduitPartType.SEGMENT, ConduitShapeHelper.SEGMENT);
		map.put(ConduitPartType.JOINT, ConduitShapeHelper.JOINT);
		map.put(ConduitPartType.MIXED_JOINT, ConduitShapeHelper.MIXED_JOINT);
		map.put(ConduitPartType.CONNECTION, Shapes.or(ConduitShapeHelper.CONNECTION_TOP, ConduitShapeHelper.CONNECTION_BOTTOM));
	});

	public static Boxf modelBox(final ConduitPartType type) {
		return ConduitShapeHelper.toModelBox(ConduitShapeHelper.BASE_SHAPES.get(type).bounds());
	}

	public static Boxf connectionTopModelBox() {
		return ConduitShapeHelper.toModelBox(ConduitShapeHelper.CONNECTION_TOP.bounds());
	}

	public static Boxf connectionBottomModelBox() {
		return ConduitShapeHelper.toModelBox(ConduitShapeHelper.CONNECTION_BOTTOM.bounds());
	}

	public static Vector3f[] segmentOffsets(final int numSegments, @Nullable final Direction dir){
		final Vector3f[] offsets = switch (numSegments) {
		case 0 -> new Vector3f[0];
		case 1 -> new Vector3f[] {Vector3f.ZERO};
		case 2 -> new Vector3f[] {new Vector3f(1.75F/16F, 0, 0), new Vector3f(-1.75F/16F, 0, 0)};
		case 3 -> new Vector3f[] {new Vector3f(1.75F/16F, 0, -1.75F/16F), new Vector3f(-1.75F/16F, 0, -1.75F/16F), new Vector3f(0, 0, 1.75F/16F)};
		default -> throw new IllegalArgumentException("Unsupported number of segments "+numSegments);
		};
		if(dir != null && !ConduitShapeHelper.isSegmentMasterDirection(dir))
			for(final Vector3f offset:offsets)
				offset.setZ(-offset.z());
		return offsets;
	}

	public static boolean isSegmentMasterDirection(final Direction dir) {
		//This is chosen to make the configurations look good.
		//It's arbitrary and will work as long as one is chosen from each axis.
		return dir == Direction.UP || dir == Direction.EAST || dir == Direction.NORTH;
	}

	public static VoxelShape getShape(final ConduitPartType type) {
		return ConduitShapeHelper.BASE_SHAPES.get(type);
	}

	public static Boxf toModelBox(final AABB bounds) {
		return new Boxf((float) bounds.minX*16, (float) bounds.minY*16, (float) bounds.minZ*16,
				(float) bounds.maxX*16, (float) bounds.maxY*16, (float) bounds.maxZ*16);
	}

	public static Quaternion segmentRotation(final Direction facing) {
		if(facing == null)
			return Quaternion.ONE;
		//These rotations are chosen to preserve the x-alignment of the segments/joints
		//Since it is impossible to preserve both x and y alignment, y-alignment is
		//corrected for by reflecting the y-coordinate when calculating offsets
		return switch(facing) {
		case DOWN -> Vector3f.XP.rotationDegrees(180F);
		case UP -> Quaternion.ONE;
		case NORTH -> Vector3f.XP.rotationDegrees(-90F);
		case SOUTH -> Vector3f.XP.rotationDegrees(90F);
		case WEST -> {
			final Quaternion counter = Vector3f.XP.rotationDegrees(90F);
			counter.mul(Vector3f.ZP.rotationDegrees(90F));
			yield counter;
		}
		case EAST -> {
			final Quaternion clockwise = Vector3f.XP.rotationDegrees(-90F);
			clockwise.mul(Vector3f.ZP.rotationDegrees(-90F));
			yield clockwise;
		}
		default -> throw new IncompatibleClassChangeError();
		};
	}

	public static boolean isPassthrough(final Map<ConduitType, Pair<ConduitTier, ConduitConnection>> first,
			final Map<ConduitType, Pair<ConduitTier, ConduitConnection>> second) {
		return first.keySet().equals(second.keySet());
	}

	public static AABB boundMixedJoint(final Map<Direction, Integer> connections) {
		final Map<Direction, FloatFloatPair> bounds = connections.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
				entry -> ConduitShapeHelper.boundJoint(entry.getValue()),
				ConduitUtil::max, () -> new EnumMap<>(Direction.class)));
		final FloatFloatPair one = ConduitShapeHelper.boundJoint(1);

		final FloatFloatPair x = bounds.keySet().stream().filter(Direction.Axis.X::test).map(bounds::get)
				.reduce(ConduitUtil::max).orElse(one);
		final FloatFloatPair y = bounds.keySet().stream().filter(Direction.Axis.Y::test).map(bounds::get)
				.reduce(ConduitUtil::max).orElse(one);
		final FloatFloatPair z = bounds.keySet().stream().filter(Direction.Axis.Z::test).map(bounds::get)
				.reduce(ConduitUtil::max).orElse(one);
		//x size controlled by y and z face widths
		final float xBound = Math.max(y.firstFloat(), z.firstFloat());
		//y size controlled by x and z face heights
		final float yBound = Math.max(x.secondFloat(), z.secondFloat());
		//z size controled by x face width and y face height
		final float zBound = Math.max(x.firstFloat(), y.secondFloat());

		return new AABB(0.5-xBound/2, 0.5-yBound/2, 0.5-zBound/2, 0.5+xBound/2, 0.5+yBound/2, 0.5+zBound/2)
				.inflate(1/32D);
	}

	public static FloatFloatPair boundJoint(final int numConnections) {
		if(numConnections == 0)
			return FloatFloatPair.of(0, 0);
		final Vector3f[] offsets = ConduitShapeHelper.segmentOffsets(numConnections, null);
		final double maxX = Arrays.stream(offsets).mapToDouble(Vector3f::x).max().orElse(0);
		final double minX = Arrays.stream(offsets).mapToDouble(Vector3f::x).min().orElse(0);
		final double maxZ = Arrays.stream(offsets).mapToDouble(Vector3f::z).max().orElse(0);
		final double minZ = Arrays.stream(offsets).mapToDouble(Vector3f::z).min().orElse(0);
		final float conduitSize = 2/16F;
		return FloatFloatPair.of((float) (maxX-minX)+conduitSize, (float) (maxZ-minZ)+conduitSize);
	}

	public static ConduitType[] sort(final Collection<ConduitType> types) {
		return types.stream().sorted(ConduitType::compareTo).toArray(ConduitType[]::new);
	}

	public static record Boxf(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {

		public Vector3f lowerCorner() {
			return new Vector3f(this.minX, this.minY, this.minZ);
		}

		public Vector3f upperCorner() {
			return new Vector3f(this.maxX, this.maxY, this.maxZ);
		}
	}

}
