package me.superckl.conduits.client.model;

import com.mojang.math.Transformation;

import net.minecraft.client.resources.model.ModelState;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class ConduitModelTransform implements ModelState{

	private final boolean uvLock;
	private final Transformation matrix;

	public ConduitModelTransform(final ModelState parent, final Quaternionf transform) {
		final Transformation matrix = new Transformation(new Matrix4f().set(transform));
		this.matrix = parent.getRotation().compose(matrix);
		this.uvLock = parent.isUvLocked();
	}

	@Override
	public @NotNull Transformation getRotation() {
		return this.matrix;
	}

	@Override
	public boolean isUvLocked() {
		return this.uvLock;
	}

}
