package me.superckl.conduits.data.client;

import me.superckl.conduits.Conduits;
import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ConduitsItemModelProvider extends ItemModelProvider {

    public ConduitsItemModelProvider(final DataGenerator generator, final ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), Conduits.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {


    }

}
