package me.superckl.conduits;

import me.superckl.conduits.conduit.ConduitTier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModTabs {

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, Conduits.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CONDUITS_TAB = TABS.register("conduits", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + Conduits.MOD_ID + ".example"))
            .icon(() -> ModItems.CONDUITS.get(ModConduits.ENERGY.getId()).get(ConduitTier.MIDDLE).get().getDefaultInstance())
            .displayItems((params, output) -> {
                ModItems.CONDUITS.values().forEach(m -> m.values().forEach(d -> output.accept(d.get())));
                output.accept(ModItems.WRENCH.get());
            })
            .build());

}
