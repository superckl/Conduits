package me.superckl.conduits.conduit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.superckl.conduits.conduit.connection.ConnectionHelper;
import me.superckl.conduits.conduit.connection.ConnectionHelper.CapabilityConnectionHelper;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;

@RequiredArgsConstructor
public enum ConduitType implements StringRepresentable{

	ITEM("item", CapabilityConnectionHelper.forCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)),
	ENERGY("energy", CapabilityConnectionHelper.forCapability(CapabilityEnergy.ENERGY));

	private final String name;
	@Getter
	private final ConnectionHelper connectionHelper;

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public StringTag tag() {
		return StringTag.valueOf(this.name);
	}

}
