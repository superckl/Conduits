package me.superckl.conduits.util;

import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.minecraft.server.packs.resources.Resource;

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

}
