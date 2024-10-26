package me.superckl.conduits;

import me.superckl.conduits.common.block.ConduitBlock;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModBlocks {

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, Conduits.MOD_ID);

	public static final DeferredHolder<Block, ConduitBlock> CONDUIT_BLOCK = ModBlocks.BLOCKS.register("conduit", ConduitBlock::new);


	public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Conduits.MOD_ID);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConduitBlockEntity>> CONDUIT_ENTITY = ModBlocks.ENTITIES.register("conduit",
			() -> BlockEntityType.Builder.of(ConduitBlockEntity::new, ModBlocks.CONDUIT_BLOCK.get()).build(null));
}
