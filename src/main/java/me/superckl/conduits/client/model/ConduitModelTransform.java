package me.superckl.conduits.client.model;

import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;

import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;

public class ConduitModelTransform implements ModelState{

	private final boolean uvLock;
	private final Transformation matrix;

	public ConduitModelTransform(final ModelState parent, final Direction facing) {
		final Transformation matrix = new Transformation(new Matrix4f(facing.getRotation()));
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

}
