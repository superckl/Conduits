package me.superckl.conduits.common.item;

import lombok.Getter;
import me.superckl.conduits.Conduits;
import me.superckl.conduits.ModBlocks;
import me.superckl.conduits.ModItems;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.ConduitTier;
import me.superckl.conduits.conduit.ConduitType;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@Getter
public class ConduitItem extends BlockItem {

    private final DeferredHolder<ConduitType<?>, ? extends ConduitType<?>> type;
    private final ConduitTier tier;
    private final String descriptionId;


    public ConduitItem(final DeferredHolder<ConduitType<?>, ? extends ConduitType<?>> type, final ConduitTier tier) {
        super(ModBlocks.CONDUIT_BLOCK.get(), new Properties());
        this.type = type;
        this.tier = tier;
        this.descriptionId = Util.makeDescriptionId("item", ResourceLocation.fromNamespaceAndPath(Conduits.MOD_ID, "conduit/" + type.getId().getPath() + "/" + tier.getSerializedName()));
    }

    /**
     * Handles informing the conduit block entity that was placed. This is called by {@link BlockItem} after it
     * places the block
     */
    @Override
    protected boolean updateCustomBlockEntityTag(final @NotNull BlockPos pPos, final Level pLevel, @Nullable final Player pPlayer, final ItemStack pStack,
                                                 final @NotNull BlockState pState) {
        final BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity instanceof final ConduitBlockEntity conduit) {
            conduit.onPlaced(this);
            return true;
        }
        return false;
    }

    /**
     * Handles conduit "overriding", when a player right clicks on a conduit with a different type or tier,
     * it should place it into the existing conduit if possible
     */
    @Override
    public @NotNull InteractionResult onItemUseFirst(final @NotNull ItemStack stack, final UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown())
            return super.onItemUseFirst(stack, context);
        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof final ConduitBlockEntity conduit) {
            final ConduitTier tier = conduit.trySetTier(this.type.get(), this.tier).orElse(null);
            if (tier == this.tier)
                return super.onItemUseFirst(stack, context);
            level.playSound(context.getPlayer(), pos, this.getPlaceSound(level.getBlockState(pos), level, pos, context.getPlayer()), SoundSource.BLOCKS,
                    (SoundType.STONE.getVolume() + 1.0F) / 2.0F, SoundType.STONE.getPitch() * 0.8F);
            if (tier == null || context.getLevel().isClientSide)
                return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
            final Player player = context.getPlayer();
            final Item toDrop = ModItems.CONDUITS.get(this.type.getId()).get(tier).get();
            if (player != null) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                    player.addItem(toDrop.getDefaultInstance());
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                }
            } else {
                stack.shrink(1);
                context.getLevel().addFreshEntity(new ItemEntity(context.getLevel(), context.getClickLocation().x, context.getClickLocation().y, context.getClickLocation().z, toDrop.getDefaultInstance()));
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }
        return super.onItemUseFirst(stack, context);
    }

    @Override
    public @NotNull String getDescriptionId() {
        return this.descriptionId;
    }

}
