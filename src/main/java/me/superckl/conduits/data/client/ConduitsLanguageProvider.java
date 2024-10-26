package me.superckl.conduits.data.client;

import me.superckl.conduits.Conduits;
import me.superckl.conduits.ModBlocks;
import me.superckl.conduits.ModConduits;
import me.superckl.conduits.ModItems;
import me.superckl.conduits.conduit.ConduitTier;
import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ConduitsLanguageProvider extends LanguageProvider {

	public ConduitsLanguageProvider(final DataGenerator gen) {
		super(gen.getPackOutput(), Conduits.MOD_ID, "en_us");
	}

	@Override
	protected void addTranslations() {
		this.add("itemGroup.conduits", "Conduits");

		this.add("conduits.gui.connection.insert", "Insert");
		this.add("conduits.gui.connection.extract", "Extract");

		this.add("conduits.type.item", "Item");
		this.add("conduits.type.energy", "Energy");
		this.add("conduits.type.fluid", "Fluid");

		this.add("conduits.command.network.conduit", "Examing conduit at %s");
		this.add("conduits.command.type.none", "Conduit has no types!");
		this.add("conduits.command.network.type", "Conduit network for type %s: %s");
		this.add("conduits.command.network.none", "No conduit network for type %s");
		this.add("conduits.gui.connection.priority", "Priority");
		this.add("conduits.destination", "Destination Mode");
		this.add("conduits.destination.nearest", "Nearest First");
		this.add("conduits.destination.random", "Random");
		this.add("conduits.destination.priority", "Priority");
		this.add("conduits.destination.round_robin", "Round Robin");
		this.add("conduits.redstone", "Redstone Mode");
		this.add("conduits.redstone.ignored", "Always Active");
		this.add("conduits.redstone.active_signal", "Active with Signal");
		this.add("conduits.redstone.inactive_signal", "Active without Signal");
		this.add("conduits.redstone.disabled", "Never Active");

		this.addBlock(ModBlocks.CONDUIT_BLOCK, "Conduit");

		this.addItem(ModItems.CONDUITS.get(ModConduits.ENERGY.getId()).get(ConduitTier.EARLY), "Early Tier Energy Conduit");
		this.addItem(ModItems.CONDUITS.get(ModConduits.ENERGY.getId()).get(ConduitTier.MIDDLE), "Middle Tier Energy Conduit");
		this.addItem(ModItems.CONDUITS.get(ModConduits.ENERGY.getId()).get(ConduitTier.LATE), "Late Tier Energy Conduit");

		this.addItem(ModItems.CONDUITS.get(ModConduits.ITEM.getId()).get(ConduitTier.EARLY), "Early Tier Item Conduit");
		this.addItem(ModItems.CONDUITS.get(ModConduits.ITEM.getId()).get(ConduitTier.MIDDLE), "Middle Tier Item Conduit");
		this.addItem(ModItems.CONDUITS.get(ModConduits.ITEM.getId()).get(ConduitTier.LATE), "Late Tier Item Conduit");

		this.addItem(ModItems.CONDUITS.get(ModConduits.FLUID.getId()).get(ConduitTier.EARLY), "Early Tier Fluid Conduit");
		this.addItem(ModItems.CONDUITS.get(ModConduits.FLUID.getId()).get(ConduitTier.MIDDLE), "Middle Tier Fluid Conduit");
		this.addItem(ModItems.CONDUITS.get(ModConduits.FLUID.getId()).get(ConduitTier.LATE), "Late Tier Fluid Conduit");

		this.addItem(ModItems.WRENCH, "Conduit Wrench");
	}

}
