package me.superckl.conduits.common.block;

import me.superckl.conduits.ModBlocks;
import me.superckl.conduits.ModItems;
import me.superckl.conduits.conduit.ConduitType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ConduitBlock extends Block implements EntityBlock, SimpleWaterloggedBlock{

	public ConduitBlock() {
		super(Properties.of(Material.STONE).noOcclusion().isViewBlocking((x, y, z) -> false));

		BlockState def = this.defaultBlockState();
		def = def.setValue(BlockStateProperties.WATERLOGGED, false);
		this.registerDefaultState(def);
	}

	@Override
	public ItemStack getCloneItemStack(final BlockState state, final HitResult target, final BlockGetter level, final BlockPos pos,
			final Player player) {
		return level.getBlockEntity(pos, ModBlocks.CONDUIT_ENTITY.get()).map(be -> {
			// TODO check where they actually clicked and give them that kind of conduit
			for(final ConduitType type:ConduitType.values())
				if(be.hasType(type))
					return new ItemStack(ModItems.CONDUITS.get(type).get(be.getTier(type)).get());
			return null;
		}).orElseGet(() -> super.getCloneItemStack(state, target, level, pos, player));

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
		level.getBlockEntity(pos, ModBlocks.CONDUIT_ENTITY.get()).ifPresent(be -> be.onNeighborChanged(Direction.fromNormal(neighbor.subtract(pos)), level.getBlockState(neighbor).getBlock()));
		super.onNeighborChange(state, level, pos, neighbor);
	}

	@Override
	protected void createBlockStateDefinition(final Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder);
		pBuilder.add(BlockStateProperties.WATERLOGGED);
	}

	@Override
	public VoxelShape getShape(final BlockState pState, final BlockGetter level, final BlockPos pPos, final CollisionContext pContext) {
		if(level.getBlockEntity(pPos) instanceof final ConduitBlockEntity conduit) {

		}
		return Shapes.block();
	}

}
