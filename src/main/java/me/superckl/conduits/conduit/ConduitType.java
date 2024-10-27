package me.superckl.conduits.conduit;

import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.connection.ConduitConnection;
import me.superckl.conduits.conduit.connection.ConduitConnectionType;
import me.superckl.conduits.conduit.network.inventory.TransferrableQuantity;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;

@RequiredArgsConstructor
public abstract class ConduitType<T extends TransferrableQuantity> implements Comparable<ConduitType<?>> {

    private Component displayName;
    @Getter
    private ResourceLocation resourceLocation;

    public Component getDisplayName() {
        if (this.displayName == null)
            this.displayName = Component.translatable("conduits.type." + this.getResourceLocation().getPath());
        return this.displayName;
    }

    public final ConduitConnection establishConnection(final ConduitConnectionType connType,
                                                       final Direction fromConduit, @Nullable final ConduitBlockEntity owner) {
        return switch (connType) {
            case CONDUIT -> new ConduitConnection.Conduit(this);
            case INVENTORY -> this.establishConnection(fromConduit, owner);
            default -> null;
        };
    }

    public abstract boolean canConnect(final Direction dir, final BlockEntity be);

    protected abstract ConduitConnection.Inventory<T> establishConnection(final Direction dir, final ConduitBlockEntity owner);

    protected abstract Codec<? extends ConduitConnection.Inventory<T>> inventoryCodec();

    public final Codec<? extends ConduitConnection> getCodec(final ConduitConnectionType connType) {
        return switch (connType) {
            case CONDUIT -> ConduitConnection.Conduit.CODEC;
            case INVENTORY -> this.inventoryCodec();
            default -> throw new IncompatibleClassChangeError();
        };
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getResourceLocation());
    }

    public void setResourceLocation(ResourceLocation location) {
        if (this.resourceLocation != null)
            throw new IllegalStateException(("Already set resource location of conduit type!"));
        this.resourceLocation = location;
    }

    @Override
    public int compareTo(@NotNull ConduitType<?> o) {
        return this.getResourceLocation().compareTo(o.getResourceLocation());
    }
}
