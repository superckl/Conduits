package me.superckl.conduits.conduit;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;

import me.superckl.conduits.conduit.part.ConduitPart;
import net.minecraft.core.Direction;

public record ConfiguredConduit(ConduitType[] types, List<ConduitPart> joints, ConduitPart mixedJoint,
		Map<Direction, ConduitPart> connections, Multimap<Direction, ConduitPart> segments) {

}
