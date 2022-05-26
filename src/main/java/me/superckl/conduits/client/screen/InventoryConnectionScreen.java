package me.superckl.conduits.client.screen;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Streams;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import me.superckl.conduits.Conduits;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.conduit.connection.DestinationMode;
import me.superckl.conduits.conduit.connection.InventoryConnectionSettings.Setting;
import me.superckl.conduits.conduit.connection.RedstoneMode;
import me.superckl.conduits.conduit.network.inventory.InventoryConnectionMenu;
import me.superckl.conduits.conduit.network.inventory.InventoryConnectionMenu.SettingsData;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class InventoryConnectionScreen extends AbstractContainerScreen<InventoryConnectionMenu>{

	public static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation(Conduits.MOD_ID, "textures/gui/conduit_gui.png");
	public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation(Conduits.MOD_ID, "textures/gui/widgets.png");

	private static final int guiHeight = 166;

	private final TranslatableComponent insertLabel = new TranslatableComponent("conduits.gui.connection.insert");
	private final TranslatableComponent extractLabel = new TranslatableComponent("conduits.gui.connection.extract");

	private final List<AbstractWidget> widgets = new ArrayList<>();
	private final List<TabWidget> tabs = new ArrayList<>();
	private final ConduitType[] modes;
	private int modeIndex = 0;

	private EnumButton<RedstoneMode> acceptMode;
	private EnumButton<RedstoneMode> provideMode;
	private EnumButton<DestinationMode> destinationMode;
	private PriorityWidget acceptPriority;
	private PriorityWidget providePriority;

	public InventoryConnectionScreen(final InventoryConnectionMenu pMenu, final Inventory pPlayerInventory, final Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
		this.imageWidth = 176;
		this.imageHeight = InventoryConnectionScreen.guiHeight+26;
		this.inventoryLabelY = this.imageHeight-93;
		this.modes = pMenu.getTypes();
		this.titleLabelY += 27;
	}

	@Override
	protected void init() {
		super.init();
		this.widgets.clear();
		this.tabs.clear();

		//Insert mode
		this.acceptMode = this.addRenderableWidget(new EnumButton<>(this, RedstoneMode.class,
				RedstoneMode.values()[this.getActive().get(Setting.ACCEPT_REDSTONE_MODE)],
				this.leftPos+8, this.topPos+59, 16, 16, x -> this.getActive().set(Setting.ACCEPT_REDSTONE_MODE, x.ordinal())));
		//Extract mode
		this.provideMode = this.addRenderableWidget(new EnumButton<>(this, RedstoneMode.class,
				RedstoneMode.values()[this.getActive().get(Setting.PROVIDE_REDSTONE_MODE)],
				this.leftPos+this.imageWidth-8-16, this.topPos+59, 16, 16,
				x -> this.getActive().set(Setting.PROVIDE_REDSTONE_MODE, x.ordinal())));

		//Extract destination
		this.destinationMode = this.addRenderableWidget(new EnumButton<>(this, DestinationMode.class,
				DestinationMode.values()[this.getActive().get(Setting.DESTINATION_MODE)],
				this.leftPos+this.imageWidth-8-2*16-2, this.topPos+59,
				16, 16, x -> this.getActive().set(Setting.DESTINATION_MODE, x.ordinal())));

		//Priorities
		this.acceptPriority = this.addRenderableWidget(new PriorityWidget(this, this.getActive().get(Setting.ACCEPT_PRIORITY),
				this.leftPos+43, this.topPos+77, i -> this.getActive().set(Setting.ACCEPT_PRIORITY, i)));
		this.providePriority = this.addRenderableWidget(new PriorityWidget(this, this.getActive().get(Setting.PROVIDE_PRIORITY),
				this.leftPos+88, this.topPos+77, i -> this.getActive().set(Setting.PROVIDE_PRIORITY, i)));

		//Tabs
		for(int j = 0; j < this.modes.length; j++) {
			final TabWidget.Type type = j == 0 ? TabWidget.Type.LEFT : TabWidget.Type.MIDDLE;
			int xOffset =  j >= 1 ? TabWidget.Type.LEFT.getWidth() : 0;
			xOffset += j;
			for (int k = 1; k < j; k++)
				xOffset += TabWidget.Type.MIDDLE.getWidth();
			final int jf = j;
			this.tabs.add(new TabWidget(this.leftPos+1+xOffset, this.topPos, type,
					(tab, pose, x, y) -> this.renderTooltip(pose, this.modes[jf].getDisplayName(), x, y),
					x -> this.changeTab(jf), RedstoneMode.DISABLED));
		}
		this.tabs.get(this.modeIndex).setSelected(true);
		this.tabs.forEach(this::addWidget);
	}

	private SettingsData getActive() {
		return this.menu.getSettings(this.modes[this.modeIndex]);
	}

	private void changeTab(final int tab) {
		this.tabs.get(this.modeIndex).setSelected(false);
		this.modeIndex = tab;
		this.tabs.get(this.modeIndex).setSelected(true);
		this.setValues();
	}

	private void setValues() {
		this.acceptMode.setValue(RedstoneMode.values()[this.getActive().get(Setting.ACCEPT_REDSTONE_MODE)]);
		this.provideMode.setValue(RedstoneMode.values()[this.getActive().get(Setting.PROVIDE_REDSTONE_MODE)]);
		this.destinationMode.setValue(DestinationMode.values()[this.getActive().get(Setting.DESTINATION_MODE)]);
		this.acceptPriority.setValue(this.getActive().get(Setting.ACCEPT_PRIORITY));
		this.providePriority.setValue(this.getActive().get(Setting.PROVIDE_PRIORITY));
	}

	protected <T extends AbstractWidget> T addRenderableWidget(final T pWidget) {
		this.widgets.add(pWidget);
		return super.addRenderableWidget(pWidget);
	}

	@Override
	protected void renderBg(final PoseStack pPoseStack, final float pPartialTick, final int pMouseX, final int pMouseY) {
		this.renderBackground(pPoseStack);
		this.tabs.stream().filter(tab -> !tab.isSelected()).forEach(tab -> tab.render(pPoseStack, pMouseX, pMouseY, pPartialTick));

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, InventoryConnectionScreen.BACKGROUND_LOCATION);
		final int i = (this.width - this.imageWidth) / 2;
		final int j = (this.height - this.imageHeight) / 2;
		this.blit(pPoseStack, i, j+26, 0, 0, this.imageWidth, InventoryConnectionScreen.guiHeight);

		this.tabs.stream().filter(TabWidget::isSelected).forEach(tab -> tab.render(pPoseStack, pMouseX, pMouseY, pPartialTick));
	}

	@Override
	public void render(final PoseStack pPoseStack, final int pMouseX, final int pMouseY, final float pPartialTick) {
		super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
		Streams.concat(this.widgets.stream(), this.tabs.stream())
		.filter(AbstractWidget::isHoveredOrFocused).forEach(w -> w.renderToolTip(pPoseStack, pMouseX, pMouseY));
	}

	@Override
	protected void renderLabels(final PoseStack pPoseStack, final int pMouseX, final int pMouseY) {
		final int leftX = this.imageWidth/4-this.font.width(this.insertLabel)/2;
		final int rightX = 3*this.imageWidth/4-this.font.width(this.extractLabel)/2;

		this.font.draw(pPoseStack, this.insertLabel, leftX, this.titleLabelY, 4210752);
		this.font.draw(pPoseStack, this.extractLabel, rightX, this.titleLabelY, 4210752);
		this.font.draw(pPoseStack, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752);
	}

}
