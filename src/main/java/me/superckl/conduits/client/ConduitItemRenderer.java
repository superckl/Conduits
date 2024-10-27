package me.superckl.conduits.client;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import me.superckl.conduits.ModBlocks;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.common.item.ConduitItem;
import me.superckl.conduits.conduit.connection.ConduitConnectionMap;
import me.superckl.conduits.conduit.connection.ConduitConnectionType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.Map;

public class ConduitItemRenderer extends BlockEntityWithoutLevelRenderer {

    public static final ConduitItemRenderer INSTANCE = new ConduitItemRenderer();
    public final ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
    private final Map<ConduitItem, ModelData> modelDataMap = Maps.newIdentityHashMap();

    public ConduitItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack pStack, ItemDisplayContext pDisplayContext, PoseStack stack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if (pStack.getItem() instanceof final ConduitItem conduit) {
            ModelData modelData = modelDataMap.computeIfAbsent(conduit, item -> {
                final ConduitConnectionMap data = ConduitConnectionMap.make();
                data.setTier(item.getType().get(), item.getTier());
                data.makeConnection(item.getType().get(), Direction.WEST, item.getType().get().establishConnection(ConduitConnectionType.CONDUIT, Direction.WEST, null));
                data.makeConnection(item.getType().get(), Direction.EAST, item.getType().get().establishConnection(ConduitConnectionType.CONDUIT, Direction.EAST, null));
                return ModelData.of(ConduitBlockEntity.CONNECTION_PROPERTY, data);
            });
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(ModBlocks.CONDUIT_BLOCK.get().defaultBlockState(),
                    stack, pBuffer, pPackedLight, pPackedOverlay, modelData, RenderType.cutoutMipped());
        }
    }

}
