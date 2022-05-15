package me.superckl.conduits.util;

import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import it.unimi.dsi.fastutil.floats.FloatFloatPair;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ConduitUtil {

	/**
	 * Parses a given resource into a Json element and closes the resource
	 */
	public static JsonElement toJson(final Resource resource){
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(resource.getInputStream());
			return JsonParser.parseReader(reader);
		}finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(resource);
		}
	}

	public static AABB rotateModelAABB(final AABB box, final Quaternion rotation) {
		final Vector3f bottom = new Vector3f((float) box.minX, (float) box.minY, (float) box.minZ);
		final Vector3f top = new Vector3f((float) box.maxX, (float) box.maxY, (float) box.maxZ);
		final Vector3f rotCenter = new Vector3f(0.5F, 0.5F, 0.5F);

		bottom.sub(rotCenter);
		top.sub(rotCenter);

		bottom.transform(rotation);
		top.transform(rotation);

		bottom.add(rotCenter);
		top.add(rotCenter);

		final Vector3f newBottom = new Vector3f(Math.min(bottom.x(), top.x()), Math.min(bottom.y(), top.y()), Math.min(bottom.z(), top.z()));
		final Vector3f newTop = new Vector3f(Math.max(bottom.x(), top.x()), Math.max(bottom.y(), top.y()), Math.max(bottom.z(), top.z()));

		return new AABB(new Vec3(newBottom), new Vec3(newTop));
	}

	public static boolean containsInclusive(final AABB box, final Vec3 loc) {
		return loc.x >= box.minX && loc.x <= box.maxX && loc.y >= box.minY && loc.y <= box.maxY && loc.z >= box.minZ && loc.z <= box.maxZ;
	}

	public static FloatFloatPair max(final FloatFloatPair pair1, final FloatFloatPair pair2) {
		return FloatFloatPair.of(Math.max(pair1.firstFloat(), pair2.firstFloat()),
				Math.max(pair1.secondFloat(), pair2.secondFloat()));
	}

}
