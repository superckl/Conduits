package me.superckl.conduits;

import java.util.EnumMap;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.superckl.conduits.common.item.ConduitItem;
import me.superckl.conduits.common.item.WrenchItem;
import me.superckl.conduits.conduit.ConduitTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Conduits.MOD_ID);

	public static final Map<ResourceLocation, Map<ConduitTier, RegistryObject<ConduitItem>>> CONDUITS = new Object2ObjectOpenHashMap<>();
	public static final RegistryObject<WrenchItem> WRENCH = ModItems.ITEMS.register("wrench", WrenchItem::new);

	static {
		ModConduits.TYPES.getEntries().forEach(obj -> {
			final Map<ConduitTier, RegistryObject<ConduitItem>> typed = ModItems.CONDUITS.computeIfAbsent(obj.getId(),
					x -> new EnumMap<>(ConduitTier.class));
			for(final ConduitTier tier:ConduitTier.values())
				typed.put(tier, ModItems.ITEMS.register(obj.getId().getPath()+"_conduit_"+tier.getSerializedName(),
						() -> new ConduitItem(obj, tier)));
		});
	}
}
