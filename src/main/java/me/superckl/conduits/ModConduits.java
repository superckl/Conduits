package me.superckl.conduits;

import java.util.function.Supplier;

import me.superckl.conduits.conduit.CapabilityConduitType;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.util.DeferredCodec;
import me.superckl.conduits.util.WarningHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class ModConduits {

	public static final DeferredRegister<ConduitType<?>> TYPES = DeferredRegister.create(new ResourceLocation(Conduits.MOD_ID,
			"conduit_types"), Conduits.MOD_ID);


	public static final Supplier<IForgeRegistry<ConduitType<?>>> TYPES_REGISTRY = ModConduits.TYPES
			.makeRegistry(WarningHelper.<Class<ConduitType<?>>>uncheckedCast(ConduitType.class), RegistryBuilder::new);

	public static final DeferredCodec<ConduitType<?>> TYPES_CODEC = new DeferredCodec<>(() -> ModConduits.TYPES_REGISTRY.get().getCodec());

	public static final RegistryObject<CapabilityConduitType.Item> ITEM = ModConduits.TYPES.register("item", CapabilityConduitType.Item::new);
	public static final RegistryObject<CapabilityConduitType.Energy> ENERGY = ModConduits.TYPES.register("energy", CapabilityConduitType.Energy::new);
	public static final RegistryObject<CapabilityConduitType.Fluid> FLUID = ModConduits.TYPES.register("fluid", CapabilityConduitType.Fluid::new);

}
