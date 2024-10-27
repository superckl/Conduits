package me.superckl.conduits.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class VectorHelper {

    public static final Vector3f ZERO = new Vector3f(0, 0, 0);
    public static final Vector3f XP = new Vector3f(1, 0, 0);

    public static Vec3 fromBlockPos(BlockPos pos) {
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    public static Vector3f copy(Vector3f vec) {
        return new Vector3f(vec.x, vec.y, vec.z);
    }

}
