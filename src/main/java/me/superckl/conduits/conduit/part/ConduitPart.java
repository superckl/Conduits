package me.superckl.conduits.conduit.part;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import lombok.Builder;
import me.superckl.conduits.conduit.ConduitTier;
import me.superckl.conduits.conduit.ConduitType;

@Builder
public record ConduitPart(ConduitPartType type, ConduitTier tier, ConduitType conduitType, Vector3f offset, Quaternion rotation) {

}
