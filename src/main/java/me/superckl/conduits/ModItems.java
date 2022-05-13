package me.superckl.conduits;

import java.util.EnumMap;
import java.util.Map;

import me.superckl.conduits.common.item.ConduitItem;
import me.superckl.conduits.conduit.ConduitTier;
import me.superckl.conduits.conduit.ConduitType;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Conduits.MOD_ID);

	public static final Map<ConduitType, Map<ConduitTier, RegistryObject<ConduitItem>>> CONDUITS = new EnumMap<>(ConduitType.class);

	static {
		for(final ConduitType type:ConduitType.values()) {
			final Map<ConduitTier, RegistryObject<ConduitItem>> typed = ModItems.CONDUITS.computeIfAbsent(type, x -> new EnumMap<>(ConduitTier.class));
			for(final ConduitTier tier:ConduitTier.values())
				typed.put(tier, ModItems.ITEMS.register(type.getSerializedName()+"_conduit_"+tier.getSerializedName(),
						() -> new ConduitItem(type, tier)));
		}
	}
}
