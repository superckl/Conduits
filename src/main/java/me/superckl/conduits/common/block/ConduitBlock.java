package me.superckl.conduits.common.block;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import me.superckl.conduits.Conduits;
import me.superckl.conduits.ModBlocks;
import me.superckl.conduits.ModItems;
import me.superckl.conduits.common.item.ConduitItem;
import me.superckl.conduits.conduit.ConduitShapeHelper;
import me.superckl.conduits.conduit.connection.ConduitConnectionMap;
import me.superckl.conduits.conduit.network.inventory.InventoryConnectionMenu;
import me.superckl.conduits.conduit.part.ConduitPartType;
import me.superckl.conduits.util.ConduitUtil;
import me.superckl.conduits.util.VectorHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

public class ConduitBlock extends Block implements EntityBlock, SimpleWaterloggedBlock{

	public ConduitBlock() {
		super(Properties.of().noOcclusion().isViewBlocking((x, y, z) -> false).dynamicShape());

		BlockState def = this.defaultBlockState();
		def = def.setValue(BlockStateProperties.WATERLOGGED, false);
		this.registerDefaultState(def);
	}

	@Override
	public @NotNull ItemStack getCloneItemStack(@NotNull BlockState state, @NotNull HitResult target, LevelReader level, @NotNull BlockPos pos, @NotNull Player player) {
		return level.getBlockEntity(pos, ModBlocks.CONDUIT_ENTITY.get()).flatMap(conduit -> conduit.getConnections().getParts().findPart(ConduitUtil.localizeHit(target.getLocation(), pos))
                .map(part -> {
                    if (part.conduitType() != null && part.tier() != null)
                        return new ItemStack(ModItems.CONDUITS.get(part.conduitType().getResourceLocation()).get(part.tier())::get);
                    return conduit.getConnections().types().findAny().map(type ->
                            new ItemStack(ModItems.CONDUITS.get(type.getResourceLocation()).get(conduit.getTier(type))::get)
                    ).orElse(null);
                })).orElse(ItemStack.EMPTY);
	}

	@Override
	protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult pHit) {
		return level.getBlockEntity(pos, ModBlocks.CONDUIT_ENTITY.get()).flatMap(conduit -> conduit.getConnections().getParts().findPart(ConduitUtil.localizeHit(pHit.getLocation(), pos))
				.filter(part -> part.type() == ConduitPartType.CONNECTION).map(part -> {
					if (!level.isClientSide && player instanceof final ServerPlayer sPlayer) {
						final Vec3 dirVector = ConduitUtil.localizeHit(pHit.getLocation(), pos).subtract(0.5, 0.5, 0.5);
						final Direction dir = Direction.getNearest(dirVector.x, dirVector.y, dirVector.z);

						sPlayer.openMenu(InventoryConnectionMenu.makeProvider(pos, dir), buf -> {
							buf.writeBlockPos(pos);
							buf.writeEnum(dir);
						});
					}
					return InteractionResult.sidedSuccess(level.isClientSide);
				})).orElseGet(() -> super.useWithoutItem(state, level, pos, player, pHit));
	}

	@Override
	public boolean isCollisionShapeFullBlock(final BlockState pState, final BlockGetter pLevel, final BlockPos pPos) {
		return false;
	}

	@Override
	public ConduitBlockEntity newBlockEntity(final BlockPos pPos, final BlockState pState) {
		return new ConduitBlockEntity(pPos, pState);
	}

	@Override
	public void onNeighborChange(final BlockState state, final LevelReader level, final BlockPos pos, final BlockPos neighbor) {
		level.getBlockEntity(pos, ModBlocks.CONDUIT_ENTITY.get()).ifPresent(be -> be.onNeighborChanged(Direction.getNearest(VectorHelper.fromBlockPos(neighbor.subtract(pos))), level.getBlockState(neighbor).getBlock()));
		super.onNeighborChange(state, level, pos, neighbor);
	}

	@Override
	protected void createBlockStateDefinition(final Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder);
		pBuilder.add(BlockStateProperties.WATERLOGGED);
	}


	@Override
	public void onRemove(final BlockState pState, final Level pLevel, final BlockPos pPos, final BlockState pNewState, final boolean pIsMoving) {
		if(!pState.is(pNewState.getBlock())) {
			final BlockEntity blockentity = pLevel.getBlockEntity(pPos);
			if (blockentity instanceof final ConduitBlockEntity conduit)
				conduit.getConnections().types().forEach(type -> {
					final DeferredHolder<Item, ConduitItem> item = ModItems.CONDUITS.get(type.getResourceLocation()).get(conduit.getTier(type));
					Containers.dropItemStack(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), new ItemStack(item::get));
				});

		}
		super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
	}

	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		Conduits.LOG.info(params.getOptionalParameter(LootContextParams.THIS_ENTITY));
		return super.getDrops(state, params);
	}

	//Global cache for connection map -> shape since the computation is expensive
	private static final Map<ConduitConnectionMap, VoxelShape> SHAPE_CACHE = ConduitConnectionMap.newConduitCache(true);

	@Override
	public @NotNull VoxelShape getShape(final BlockState pState, final BlockGetter level, final BlockPos pPos, @Nullable final CollisionContext pContext) {
		if(level.getBlockEntity(pPos) instanceof final ConduitBlockEntity conduit)
			return ConduitUtil.copyComputeIfAbsent(ConduitBlock.SHAPE_CACHE, conduit.getConnections(), connections -> connections.getParts().getShape());
		return ConduitShapeHelper.getShape(ConduitPartType.JOINT);
	}

	@Override
	public @NotNull VoxelShape getInteractionShape(final BlockState pState, final BlockGetter pLevel, final BlockPos pPos) {
		return this.getShape(pState, pLevel, pPos, null);
	}

}
