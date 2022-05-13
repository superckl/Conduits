package me.superckl.conduits.client.model;

import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;

import net.minecraft.client.resources.model.ModelState;

public class ConduitModelTransform implements ModelState{

	private final boolean uvLock;
	private final Transformation matrix;

	public ConduitModelTransform(final ModelState parent, final Quaternion transform) {
		final Transformation matrix = new Transformation(new Matrix4f(transform));
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
