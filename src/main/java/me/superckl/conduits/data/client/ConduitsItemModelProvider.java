package me.superckl.conduits.data.client;

import me.superckl.conduits.Conduits;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ConduitsItemModelProvider extends ItemModelProvider{

	public ConduitsItemModelProvider(final DataGenerator generator, final ExistingFileHelper existingFileHelper) {
		super(generator, Conduits.MOD_ID, existingFileHelper);
	}

	@Override
	protected void registerModels() {


	}

}
