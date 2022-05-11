package me.superckl.conduits.common.item;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import lombok.Getter;
import me.superckl.conduits.ConduitTier;
import me.superckl.conduits.ConduitType;
import me.superckl.conduits.Conduits;
import me.superckl.conduits.ModBlocks;
import me.superckl.conduits.ModItems;
import me.superckl.conduits.client.ConduitItemRenderer;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.common.block.ConduitBlockEntity.ConnectionData;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;

@Getter
public class ConduitItem extends BlockItem{

	private final ConduitType type;
	private final ConduitTier tier;
	private final String descriptionId;

	@Getter
	private final IModelData renderData;

	public ConduitItem(final ConduitType type, final ConduitTier tier) {
		super(ModBlocks.CONDUIT_BLOCK.get(), new Properties().tab(Conduits.CONDUIT_TAB));
		this.type = type;
		this.tier = tier;
		this.descriptionId = Util.makeDescriptionId("item", new ResourceLocation(Conduits.MOD_ID, "conduit/"+type.getSerializedName()+"/"+tier.getSerializedName()));

		final ConnectionData data = ConnectionData.make();
		data.setTier(type, tier);
		this.renderData = new ModelDataMap.Builder().withInitial(ConduitBlockEntity.CONNECTION_PROPERTY, data).build();
	}

	/**
	 * Handles informing the conduit block entity that was placed. This is called by {@link BlockItem} after it
	 * places the block
	 */
	@Override
	protected boolean updateCustomBlockEntityTag(final BlockPos pPos, final Level pLevel, @Nullable final Player pPlayer, final ItemStack pStack,
			final BlockState pState) {
		final BlockEntity blockentity = pLevel.getBlockEntity(pPos);
		if(blockentity instanceof final ConduitBlockEntity conduit) {
			conduit.onPlaced(this);
			return true;
		}
		return false;
	}

	/**
	 * Handles conduit "overriding", when a player right clicks on a conduit with a different type or tier,
	 * it should place it into the existing conduit if possible
	 */
	@SuppressWarnings("resource")
	@Override
	public InteractionResult onItemUseFirst(final ItemStack stack, final UseOnContext context) {
		final BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
		if(be instanceof final ConduitBlockEntity conduit) {
			conduit.trySetTier(this.type, this.tier).ifPresent(tier -> {
				if(tier == this.tier || context.getLevel().isClientSide)
					return;

				final Player player = context.getPlayer();
				final Item toDrop = ModItems.CONDUITS.get(this.type).get(tier).get();
				if(player != null) {
					if(!player.isCreative())
						stack.shrink(1);
					player.getInventory().placeItemBackInInventory(toDrop.getDefaultInstance());
				}else {
					stack.shrink(1);
					context.getLevel().addFreshEntity(new ItemEntity(context.getLevel(), context.getClickLocation().x, context.getClickLocation().y, context.getClickLocation().z, toDrop.getDefaultInstance()));
				}
			});
			return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
		}
		return super.onItemUseFirst(stack, context);
	}

	@Override
	public String getDescriptionId() {
		return this.descriptionId;
	}

	@Override
	public void fillItemCategory(final CreativeModeTab pGroup, final NonNullList<ItemStack> pItems) {
		if(this.allowdedIn(pGroup))
			pItems.add(this.getDefaultInstance());
	}

	@Override
	public void initializeClient(final Consumer<IItemRenderProperties> consumer) {
		consumer.accept(new IItemRenderProperties() {

			@Override
			public ConduitItemRenderer getItemStackRenderer() {
				return ConduitItemRenderer.INSTANCE;
			}
		});
	}

}
