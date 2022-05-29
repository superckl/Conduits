package me.superckl.conduits.conduit;

import java.util.Objects;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.connection.ConduitConnection;
import me.superckl.conduits.conduit.connection.ConduitConnectionType;
import me.superckl.conduits.conduit.network.inventory.TransferrableQuantity;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class ConduitType<T extends TransferrableQuantity> extends ForgeRegistryEntry<ConduitType<?>> implements Comparable<ConduitType<T>>{

	private Component displayName;

	public Component getDisplayName() {
		if(this.displayName == null)
			this.displayName = new TranslatableComponent("conduits.type."+this.getRegistryName().getPath());
		return this.displayName;
	}

	public final ConduitConnection establishConnection(final ConduitConnectionType connType,
			final Direction fromConduit, @Nullable final ConduitBlockEntity owner){
		return switch(connType) {
		case CONDUIT -> new ConduitConnection.Conduit(this);
		case INVENTORY -> this.establishConnection(fromConduit, owner);
		default -> null;
		};
	}

	public abstract boolean canConnect(final Direction dir, final BlockEntity be);
	protected abstract ConduitConnection.Inventory<T> establishConnection(final Direction dir, final ConduitBlockEntity owner);
	protected abstract Codec<? extends ConduitConnection.Inventory<T>> inventoryCodec();

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
	public int compareTo(final ConduitType<T> o) {
		return this.getRegistryName().compareTo(o.getRegistryName());
	}

}
