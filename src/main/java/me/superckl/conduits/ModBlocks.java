package me.superckl.conduits;

import me.superckl.conduits.common.block.ConduitBlock;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Conduits.MOD_ID);

	public static final RegistryObject<ConduitBlock> CONDUIT_BLOCK = ModBlocks.BLOCKS.register("conduit", ConduitBlock::new);


	public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Conduits.MOD_ID);

	public static final RegistryObject<BlockEntityType<ConduitBlockEntity>> CONDUIT_ENTITY = ModBlocks.ENTITIES.register("conduit",
			() -> BlockEntityType.Builder.of(ConduitBlockEntity::new, ModBlocks.CONDUIT_BLOCK.get()).build(null));
}
