package me.superckl.conduits.conduit;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import me.superckl.conduits.conduit.connection.ConduitConnectionType;
import me.superckl.conduits.conduit.part.ConduitPartType;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ConduitShapeHelper {

	private static final VoxelShape SEGMENT = Shapes.box(7/16D, 9.5/16D, 7/16D, 9/16D, 16/16D, 9/16D);
	private static final VoxelShape JOINT = Shapes.box(6.5/16D, 6.5/16D, 6.5/16D, 9.5/16D, 9.5/16D, 9.5/16D);
	private static final VoxelShape MIXED_JOINT = Shapes.box(4/16D, 4/16D, 4/16D, 12/16D, 12/16D, 12/16D);
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

	public static Vector3f[] segmentOffsets(final int numSegments){
		switch(numSegments) {
		case 0:
			return new Vector3f[0];
		case 1:
			return new Vector3f[] {Vector3f.ZERO};
		case 2:
			return new Vector3f[] {new Vector3f(2/16F, 0, 0), new Vector3f(-2/16F, 0, 0)};
		default:
			throw new IllegalArgumentException("Unsupported number of segments "+numSegments);
		}
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
		//These rotations are chosen to align conduits from adjacent blocks properly
		switch(facing) {
		case DOWN:
			return Vector3f.XP.rotationDegrees(180.0F);
		case UP:
			return Quaternion.ONE;
		case NORTH:
			return Vector3f.XP.rotationDegrees(-90.0F);
		case SOUTH:
			return Vector3f.XP.rotationDegrees(90.0F);
		case WEST:
			final Quaternion counter = Vector3f.XP.rotationDegrees(90.0F);
			counter.mul(Vector3f.ZP.rotationDegrees(90.0F));
			return counter;
		case EAST:
			final Quaternion clockwise = Vector3f.XP.rotationDegrees(-90.0F);
			clockwise.mul(Vector3f.ZP.rotationDegrees(-90.0F));
			return clockwise;
		default:
			throw new IncompatibleClassChangeError();
		}
	}

	public static boolean isPassthrough(final Map<ConduitType, Pair<ConduitTier, ConduitConnectionType>> first,
			final Map<ConduitType, Pair<ConduitTier, ConduitConnectionType>> second) {
		return first.keySet().equals(second.keySet());
	}

	public static ConduitType[] sort(final Collection<ConduitType> types) {
		return types.stream().sorted(ConduitType::compareTo).toArray(ConduitType[]::new);
	}

	public static record Boxf(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {}

}
