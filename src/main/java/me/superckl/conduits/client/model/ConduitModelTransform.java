package me.superckl.conduits.client.model;

import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;

import me.superckl.conduits.PartType;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;

public class ConduitModelTransform implements ModelState{

	private final boolean uvLock;
	private final Transformation matrix;

	public ConduitModelTransform(final ModelState parent, final Direction facing, final PartType type) {
		final Transformation matrix;
		if(type == PartType.SEGMENT)
			matrix = new Transformation(new Matrix4f(this.getSegmentRotation(facing)));
		else if(type == PartType.JOINT)
			matrix = new Transformation(new Matrix4f(this.getJointRotation(facing)));
		else
			matrix = Transformation.identity();
		this.matrix = parent.getRotation().compose(matrix);
		this.uvLock = parent.isUvLocked();
	}

	@Override
	public Transformation getRotation() {
		return this.matrix;
	}

	@Override
	public boolean isUvLocked() {
		return this.uvLock;
	}

	private Quaternion getSegmentRotation(final Direction facing) {
		//These rotations are chosen to align conduits from adjacent blocks properly
		switch(facing) {
		case DOWN:
			return Vector3f.XP.rotationDegrees(180.0F);
		case UP:
			return Quaternion.ONE.copy();
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

	private Quaternion getJointRotation(final Direction facing) {
		return this.getSegmentRotation(facing);
	}

}
