package me.superckl.conduits;

import com.mojang.serialization.Codec;
import me.superckl.conduits.conduit.CapabilityConduitType;
import me.superckl.conduits.conduit.ConduitType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.callback.AddCallback;

public class ModConduits {

    public static final DeferredRegister<ConduitType<?>> TYPES = DeferredRegister.create(ResourceLocation.fromNamespaceAndPath(Conduits.MOD_ID,
            "conduit_types"), Conduits.MOD_ID);


    public static final Registry<ConduitType<?>> TYPES_REGISTRY = ModConduits.TYPES.makeRegistry(b -> {
        b.callback((AddCallback<ConduitType<?>>) (registry, id, key, value) -> value.setResourceLocation(key.location()));
    });

    public static final Codec<ConduitType<?>> TYPES_CODEC = ModConduits.TYPES_REGISTRY.byNameCodec();

    public static final DeferredHolder<ConduitType<?>, CapabilityConduitType.Item> ITEM = ModConduits.TYPES.register("item", CapabilityConduitType.Item::new);
    public static final DeferredHolder<ConduitType<?>, CapabilityConduitType.Energy> ENERGY = ModConduits.TYPES.register("energy", CapabilityConduitType.Energy::new);
    public static final DeferredHolder<ConduitType<?>, CapabilityConduitType.Fluid> FLUID = ModConduits.TYPES.register("fluid", CapabilityConduitType.Fluid::new);

}
