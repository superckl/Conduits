package me.superckl.conduits.conduit;

import java.util.Objects;

import com.mojang.serialization.Codec;

import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.connection.ConduitConnection;
import me.superckl.conduits.conduit.connection.ConduitConnectionType;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class ConduitType extends ForgeRegistryEntry<ConduitType> implements Comparable<ConduitType>{

	private Component displayName;

	public Component getDisplayName() {
		if(this.displayName == null)
			this.displayName = new TranslatableComponent("conduits.type."+this.getRegistryName().getPath());
		return this.displayName;
	}

	public abstract boolean canConnect(final Direction dir, final BlockEntity be);
	/**
	 * This should only be called through {@link ConduitConnectionType#apply}
	 */
	@Deprecated
	public abstract ConduitConnection.Inventory establishConnection(final Direction dir, final ConduitBlockEntity owner);
	protected abstract Codec<? extends ConduitConnection.Inventory> inventoryCodec();

	public final Codec<? extends ConduitConnection> getCodec(final ConduitConnectionType connType){
		return switch(connType) {
		case CONDUIT -> ConduitConnection.Conduit.CODEC;
		case INVENTORY -> this.inventoryCodec();
		default -> throw new IncompatibleClassChangeError();
		};
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getRegistryName());
	}

	@Override
	public int compareTo(final ConduitType o) {
		return this.getRegistryName().compareTo(o.getRegistryName());
	}

}
