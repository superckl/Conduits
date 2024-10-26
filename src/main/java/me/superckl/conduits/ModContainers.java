package me.superckl.conduits;

import me.superckl.conduits.conduit.network.inventory.InventoryConnectionMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModContainers {

	public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, Conduits.MOD_ID);

	public static final DeferredHolder<MenuType<?>, MenuType<InventoryConnectionMenu>> INVENTORY_CONNECTION = ModContainers.MENU_TYPES.register("inventory_connection",
			() -> IMenuTypeExtension.create(InventoryConnectionMenu::new));

}
