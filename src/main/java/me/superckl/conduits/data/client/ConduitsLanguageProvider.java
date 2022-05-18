package me.superckl.conduits.data.client;

import me.superckl.conduits.Conduits;
import me.superckl.conduits.ModBlocks;
import me.superckl.conduits.ModItems;
import me.superckl.conduits.conduit.ConduitTier;
import me.superckl.conduits.conduit.ConduitType;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class ConduitsLanguageProvider extends LanguageProvider{

	public ConduitsLanguageProvider(final DataGenerator gen) {
		super(gen, Conduits.MOD_ID, "en_us");
	}

	@Override
	protected void addTranslations() {
		this.add("itemGroup.conduits", "Conduits");

		this.add("conduits.command.network.conduit", "Examing conduit at %s");
		this.add("conduits.command.type.none", "Conduit has no types!");
		this.add("conduits.command.network.type", "Conduit network for type %s: %s");
		this.add("conduits.command.network.none", "No conduit network for type %s");

		this.addBlock(ModBlocks.CONDUIT_BLOCK, "Conduit");

		this.addItem(ModItems.CONDUITS.get(ConduitType.ENERGY).get(ConduitTier.EARLY), "Early Tier Energy Conduit");
		this.addItem(ModItems.CONDUITS.get(ConduitType.ENERGY).get(ConduitTier.MIDDLE), "Middle Tier Energy Conduit");
		this.addItem(ModItems.CONDUITS.get(ConduitType.ENERGY).get(ConduitTier.LATE), "Late Tier Energy Conduit");

		this.addItem(ModItems.CONDUITS.get(ConduitType.ITEM).get(ConduitTier.EARLY), "Early Tier Item Conduit");
		this.addItem(ModItems.CONDUITS.get(ConduitType.ITEM).get(ConduitTier.MIDDLE), "Middle Tier Item Conduit");
		this.addItem(ModItems.CONDUITS.get(ConduitType.ITEM).get(ConduitTier.LATE), "Late Tier Item Conduit");
		
		this.addItem(ModItems.CONDUITS.get(ConduitType.FLUID).get(ConduitTier.EARLY), "Early Tier Fluid Conduit");
		this.addItem(ModItems.CONDUITS.get(ConduitType.FLUID).get(ConduitTier.MIDDLE), "Middle Tier Fluid Conduit");
		this.addItem(ModItems.CONDUITS.get(ConduitType.FLUID).get(ConduitTier.LATE), "Late Tier Fluid Conduit");

		this.addItem(ModItems.WRENCH, "Conduit Wrench");
	}

}
