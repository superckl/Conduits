package me.superckl.conduits.common.item;

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
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

public class WrenchItem extends Item{

	public WrenchItem() {
		super(new Properties().stacksTo(1));
	}

	@Override
	public @NotNull InteractionResult onItemUseFirst(final @NotNull ItemStack stack, final UseOnContext context) {
		if(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof final ConduitBlockEntity conduit) {
			final BlockPos pos = context.getClickedPos();

			return conduit.getConnections().getParts().findPart(ConduitUtil.localizeHit(context.getClickLocation(), pos)).map(part -> {
				if(part.type() == ConduitPartType.MIXED_JOINT || part.type() == ConduitPartType.CONNECTION) {
					context.getLevel().removeBlock(pos, false);
					return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
				}
				if(conduit.removeType(part.conduitType())) {
					final DeferredHolder<Item, ConduitItem> item = ModItems.CONDUITS.get(part.conduitType().getResourceLocation()).get(part.tier());
					Containers.dropItemStack(context.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(item::get));
					return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
				}
				return null;
			}).orElseGet(() -> super.onItemUseFirst(stack, context));
		}
		return super.onItemUseFirst(stack, context);
	}

}
