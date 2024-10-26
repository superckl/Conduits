package me.superckl.conduits.conduit.network.inventory;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import me.superckl.conduits.ModBlocks;
import me.superckl.conduits.ModContainers;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.conduit.connection.ConduitConnection;
import me.superckl.conduits.conduit.connection.ConduitConnectionType;
import me.superckl.conduits.conduit.connection.InventoryConnectionSettings;
import me.superckl.conduits.packets.SyncConduitSettingPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class InventoryConnectionMenu extends AbstractContainerMenu{

	private final ConduitBlockEntity conduit;
	private final Direction dir;
	private final Map<ConduitType<?>, SettingsData> types;

	public InventoryConnectionMenu(final int pContainerId, final Inventory inv, final FriendlyByteBuf buf) {
		this(pContainerId, inv, buf.readBlockPos(), buf.readEnum(Direction.class));
	}

	public InventoryConnectionMenu(final int pContainerId, final Inventory inv, final BlockPos pos, final Direction dir) {
		super(ModContainers.INVENTORY_CONNECTION.get(), pContainerId);
		this.dir = dir;
		this.conduit = inv.player.level().getBlockEntity(pos, ModBlocks.CONDUIT_ENTITY.get()).orElse(null);

		if(this.conduit == null)
			this.types = Collections.emptyMap();
		else
			this.types = this.conduit.getConnections().types().filter(type -> this.conduit.getConnections().hasConnection(type, this.dir))
			.map(type -> this.conduit.getConnections().getConnection(type, this.dir))
			.filter(con -> con.getConnectionType() == ConduitConnectionType.INVENTORY).map(ConduitConnection::asInventory)
			.collect(Collectors.toMap(ConduitConnection::getType, invCon -> {
				InventoryConnectionSettings settings = invCon.getSettings();
				if(inv.player.level().isClientSide)
					settings = settings.copy(x -> this.slotsChanged(null));
				return new SettingsData(settings);
			}, (x,y) -> {throw new UnsupportedOperationException();}, Object2ObjectOpenHashMap::new));

		this.types.values().forEach(this::addDataSlots);

		for(int j = 0; j < 3; ++j)
			for(int i = 0; i < 9; ++i)
				this.addSlot(new Slot(inv, i + (j + 1) * 9, 8 + i * 18, j * 18 + 110));

		for(int i = 0; i < 9; ++i)
			this.addSlot(new Slot(inv, i, 8 + i * 18, 168));

		this.addSlotListener(new ContainerListener() {

			@Override
			public void slotChanged(final @NotNull AbstractContainerMenu pContainerToSend, final int pSlotInd, final @NotNull ItemStack pStack) {}

			@Override
			public void dataChanged(final @NotNull AbstractContainerMenu pContainerMenu, final int pDataSlotIndex, final int pValue) {
				PacketDistributor.sendToServer(new SyncConduitSettingPacket.Data(pDataSlotIndex, pValue));
			}
		});

	}

	public SettingsData getSettings(final ConduitType<?> type) {
		return this.types.get(type);
	}

	public ConduitType<?>[] getTypes() {
		return this.types.keySet().toArray(ConduitType[]::new);
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(final @NotNull Player pPlayer) {
		return this.conduit != null && !this.conduit.isRemoved();
	}

	@RequiredArgsConstructor
	public static class SettingsData implements ContainerData {

		private final InventoryConnectionSettings settings;

		@Override
		public int get(final int pIndex) {
			return this.get(InventoryConnectionSettings.Setting.values()[pIndex]);
		}

		public int get(final InventoryConnectionSettings.Setting setting) {
			return setting.getSyncHelper().get(this.settings);
		}

		@Override
		public void set(final int pIndex, final int pValue) {
			this.set(InventoryConnectionSettings.Setting.values()[pIndex], pValue);
		}

		public void set(final InventoryConnectionSettings.Setting setting, final int value) {
			setting.getSyncHelper().setFor(this.settings, value);
		}

		@Override
		public int getCount() {
			return InventoryConnectionSettings.Setting.values().length;
		}

	}

	public static MenuProvider makeProvider(final BlockPos pos, final Direction dir) {
		return new MenuProvider() {

			@Override
			public AbstractContainerMenu createMenu(final int pContainerId, final @NotNull Inventory pInventory, final @NotNull Player pPlayer) {
				return new InventoryConnectionMenu(pContainerId, pInventory, pos, dir);
			}

			@Override
			public @NotNull Component getDisplayName() {
				return Component.translatable("conduit.gui.connection.title");
			}
		};
	}

}
