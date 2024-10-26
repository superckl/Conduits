package me.superckl.conduits.conduit.connection;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.superckl.conduits.client.screen.ButtonImageProvider;
import me.superckl.conduits.conduit.connection.ConduitConnection.Inventory;
import me.superckl.conduits.conduit.network.ConduitNetwork;
import me.superckl.conduits.conduit.network.Distributor;
import me.superckl.conduits.conduit.network.inventory.TransferrableQuantity;
import me.superckl.conduits.util.Positioned;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
@Getter
public enum DestinationMode implements ButtonImageProvider, StringRepresentable{

	NEAREST("nearest", 0, 0, CachedSorter.DistanceSorter::makeDistributor),
	RANDOM("random", 0, 0, CachedSorter.RandomSorter::makeDistributor),
	PRIORITY("priority", 0, 0, CachedSorter.AcceptPrioritySorter::makeDistributor),
	ROUND_ROBIN("round_robin", 0, 0, null);

	private static final Map<String, DestinationMode> BY_NAME = Arrays.stream(DestinationMode.values()).collect(Collectors.toMap(StringRepresentable::getSerializedName, v -> v));
	public static final Codec<DestinationMode> CODEC = StringRepresentable.fromEnum(DestinationMode::values);

	private final String name;
	private final int texX;
	private final int texY;
	private List<Component> tooltip;
	@Getter
	private final DistributorFactory factory;

	@Override
	public @NotNull String getSerializedName() {
		return this.name;
	}

	@Override
	public List<Component> getTooltip(){
		if(this.tooltip == null)
			this.tooltip = ImmutableList.of(Component.translatable("conduits.destination").withStyle(ChatFormatting.WHITE),
					Component.translatable("conduits.destination."+this.name).withStyle(ChatFormatting.GRAY));
		return this.tooltip;
	}

	@FunctionalInterface
	public static interface DistributorFactory{

		<T extends TransferrableQuantity, V extends Inventory<T>> Distributor<T, V> create(ConduitNetwork<T> network, Positioned<V> provider);

	}

}
