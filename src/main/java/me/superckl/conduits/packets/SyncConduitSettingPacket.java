package me.superckl.conduits.packets;

import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import me.superckl.conduits.conduit.network.inventory.InventoryConnectionMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

@RequiredArgsConstructor
public class SyncConduitSettingPacket{

	private final int settingIndex;
	private final int settingValue;

	public SyncConduitSettingPacket(final FriendlyByteBuf buf) {
		this.settingIndex = buf.readVarInt();
		this.settingValue = buf.readVarInt();
	}

	public void handle(final Supplier<NetworkEvent.Context> ctx) {
		final ServerPlayer player = ctx.get().getSender();
		if(player != null)
			ctx.get().enqueueWork(() -> {
				if(player.containerMenu instanceof final InventoryConnectionMenu menu)
					menu.setData(this.settingIndex, this.settingValue);
			});
		ctx.get().setPacketHandled(true);
	}

	public void write(final FriendlyByteBuf buf) {
		buf.writeVarInt(this.settingIndex);
		buf.writeVarInt(this.settingValue);
	}

}
