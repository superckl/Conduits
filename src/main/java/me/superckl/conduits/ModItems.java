package me.superckl.conduits;

import java.util.EnumMap;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.superckl.conduits.common.item.ConduitItem;
import me.superckl.conduits.common.item.WrenchItem;
import me.superckl.conduits.conduit.ConduitTier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Conduits.MOD_ID);

	public static final Map<ResourceLocation, Map<ConduitTier, DeferredHolder<Item, ConduitItem>>> CONDUITS = new Object2ObjectOpenHashMap<>();
	public static final DeferredHolder<Item, WrenchItem> WRENCH = ModItems.ITEMS.register("wrench", WrenchItem::new);

	static {
		ModConduits.TYPES.getEntries().forEach(obj -> {
			final Map<ConduitTier, DeferredHolder<Item, ConduitItem>> typed = ModItems.CONDUITS.computeIfAbsent(obj.getId(),
					x -> new EnumMap<>(ConduitTier.class));
			for(final ConduitTier tier:ConduitTier.values())
				typed.put(tier, ModItems.ITEMS.register(obj.getId().getPath()+"_conduit_"+tier.getSerializedName(),
						() -> new ConduitItem(obj, tier)));
		});
	}
}
