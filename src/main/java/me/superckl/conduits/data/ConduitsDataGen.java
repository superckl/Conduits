package me.superckl.conduits.data;

import me.superckl.conduits.Conduits;
import me.superckl.conduits.data.client.ConduitsBlockStateProvider;
import me.superckl.conduits.data.client.ConduitsItemModelProvider;
import me.superckl.conduits.data.client.ConduitsLanguageProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Conduits.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ConduitsDataGen {

    private ConduitsDataGen() {
    }

    @SubscribeEvent
    public static void gatherData(final GatherDataEvent e) {
        final var gen = e.getGenerator();
        final var fileHelper = e.getExistingFileHelper();

        gen.addProvider(e.includeClient(), new ConduitsItemModelProvider(gen, fileHelper));
        gen.addProvider(e.includeClient(), new ConduitsBlockStateProvider(gen, fileHelper));
        gen.addProvider(e.includeClient(), new ConduitsLanguageProvider(gen));

        if (e.includeServer()) {

        }
    }

}
