package me.superckl.conduits.packets;

import java.util.function.Supplier;

import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import me.superckl.conduits.Conduits;
import me.superckl.conduits.conduit.network.inventory.InventoryConnectionMenu;
import net.minecraft.client.renderer.entity.layers.SpiderEyesLayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class SyncConduitSettingPacket{

	public record Data(int settingIndex, int settingValue) implements CustomPacketPayload{

		public static final StreamCodec<ByteBuf, Data> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Data::settingIndex, ByteBufCodecs.VAR_INT, Data::settingValue, Data::new);

		public static final CustomPacketPayload.Type<Data> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Conduits.MOD_ID, "conduit_settings"));

		@Override
		public @NotNull Type<Data> type() {
			return TYPE;
		}
	}

	public static void handleServer(Data data, IPayloadContext context){
		Player player = context.player();
		context.enqueueWork(() -> {
			if(player.containerMenu instanceof InventoryConnectionMenu menu)
				menu.setData(data.settingIndex, data.settingValue);
		});
	}

}


