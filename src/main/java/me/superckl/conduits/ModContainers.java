package me.superckl.conduits;

import me.superckl.conduits.conduit.network.inventory.InventoryConnectionMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModContainers {

	public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, Conduits.MOD_ID);

	public static final RegistryObject<MenuType<InventoryConnectionMenu>> INVENTORY_CONNECTION = ModContainers.MENU_TYPES.register("inventory_connection",
			() -> new MenuType<>(InventoryConnectionMenu::new));

}
