package me.superckl.conduits.common.item;

import me.superckl.conduits.Conduits;
import me.superckl.conduits.ModItems;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.part.ConduitPartType;
import me.superckl.conduits.util.ConduitUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.registries.RegistryObject;

public class WrenchItem extends Item{

	public WrenchItem() {
		super(new Properties().stacksTo(1).tab(Conduits.CONDUIT_TAB));
	}

	@SuppressWarnings("resource")
	@Override
	public InteractionResult onItemUseFirst(final ItemStack stack, final UseOnContext context) {
		if(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof final ConduitBlockEntity conduit) {
			final BlockPos pos = context.getClickedPos();

			return conduit.getConnections().getParts().findPart(ConduitUtil.localizeHit(context.getClickLocation(), pos)).map(part -> {
				if(part.type() == ConduitPartType.MIXED_JOINT || part.type() == ConduitPartType.CONNECTION) {
					context.getLevel().removeBlock(pos, false);
					return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
				}
				if(conduit.removeType(part.conduitType())) {
					final RegistryObject<ConduitItem> item = ModItems.CONDUITS.get(part.conduitType().getRegistryName()).get(part.tier());
					Containers.dropItemStack(context.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(item::get));
					return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
				}
				return null;
			}).orElseGet(() -> super.onItemUseFirst(stack, context));
		}
		return super.onItemUseFirst(stack, context);
	}

}
