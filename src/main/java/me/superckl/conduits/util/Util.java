package me.superckl.conduits.util;

import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Util {

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

}