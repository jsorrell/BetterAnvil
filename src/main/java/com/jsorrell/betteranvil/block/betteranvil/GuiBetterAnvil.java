package com.jsorrell.betteranvil.block.betteranvil;

import com.jsorrell.betteranvil.ModidPacketHandler;
import com.jsorrell.betteranvil.block.ModBlocks;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiBetterAnvil extends GuiContainer {
	private InventoryPlayer playerInv;
	//TODO: replace with custom resource?
	private static final ResourceLocation BG_TEXTURE = new ResourceLocation("textures/gui/container/anvil.png");
	private final ContainerBetterAnvil container;
	private GuiTextField nameField;

	public GuiBetterAnvil(Container container, InventoryPlayer playerInv) {
		super(container);
		this.playerInv = playerInv;
		this.container = (ContainerBetterAnvil) container;
	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.nameField = new GuiTextField(0, this.fontRenderer, i + 62, j + 24, 103, 12);
		this.nameField.setTextColor(-1);
		this.nameField.setDisabledTextColour(-1);
		this.nameField.setEnableBackgroundDrawing(false);
		this.nameField.setMaxStringLength(35);
		this.container.gui = this;//TODO: coding style?
		updateNameInput();
		setNameInputEnabled(this.container.getSlot(0).getHasStack());
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1, 1, 1, 1);
		mc.getTextureManager().bindTexture(BG_TEXTURE);
		int x = (this.width - this.xSize) / 2;
		int y = (this.height - this.ySize) / 2;
		//Draw background
		drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
		//Draw text box
		drawTexturedModalRect(x + 59, y + 20, 0, this.ySize + (this.container.getSlot(0).getHasStack() ? 0 : 16), 110, 16);
		//Draw X on arrow for invalid anvil operation
		if ((this.container.getSlot(0).getHasStack() || this.container.getSlot(1).getHasStack()) && !this.container.getSlot(2).getHasStack()) {
			this.drawTexturedModalRect(x + 99, y + 45, this.xSize, 0, 28, 21);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		GlStateManager.disableLighting();
		GlStateManager.disableBlend();

		//Draw anvil name
		String name = I18n.format(ModBlocks.anvil.getUnlocalizedName() + ".name");
		fontRenderer.drawString(name, xSize / 2 - fontRenderer.getStringWidth(name) / 2, 6, 0x404040);

		//Draw xp message
		if (this.container.xpCost > 0) {
			int xpMessageColor = 0x80ff20;
			boolean validOutput = true;
			String xpMessage = I18n.format("container.repair.cost", this.container.xpCost);

			if (this.container.xpCost >= 40 && !this.mc.player.capabilities.isCreativeMode) {
				xpMessage = I18n.format("container.repair.expensive");
				xpMessageColor = 0xff6060;
			} else if (!this.container.getSlot(2).getHasStack()) {
				validOutput = false;
			} else if (!this.container.getSlot(2).canTakeStack(this.playerInv.player)) {
				xpMessageColor = 0xff6060;
			}

			if (validOutput) {
				int xpMessageShadowColor = 0xff000000 | (xpMessageColor & 0xfcfcfc00) >> 2;
				int xpMessageX = this.xSize - 8 - this.fontRenderer.getStringWidth(xpMessage);
				int xpMessageY = 67;

				if (this.fontRenderer.getUnicodeFlag()) {
					//If unicode font rendering, just outline with gray box/black outline
					drawRect(xpMessageX - 3, 65, this.xSize - 7, 77, 0xff000000);
					drawRect(xpMessageX - 2, 66, this.xSize - 8, 76, 0xff3b3b3b);
				} else {
					//If not unicode font rendering, give a drop shadow to the xp message
					this.fontRenderer.drawString(xpMessage, xpMessageX, xpMessageY + 1, xpMessageShadowColor);
					this.fontRenderer.drawString(xpMessage, xpMessageX + 1, xpMessageY, xpMessageShadowColor);
					this.fontRenderer.drawString(xpMessage, xpMessageX + 1, xpMessageY + 1, xpMessageShadowColor);
				}
				this.fontRenderer.drawString(xpMessage, xpMessageX, 67, xpMessageColor);
			}
		}

		GlStateManager.enableLighting();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (this.nameField.textboxKeyTyped(typedChar, keyCode)) {
			this.renameItem();
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}

	private void renameItem() {
		String nameField = this.nameField.getText();
		Slot slot = this.container.getSlot(0);

		if (slot.getHasStack() && !slot.getStack().hasDisplayName() && nameField.equals(slot.getStack().getDisplayName())) {
			nameField = "";
		}

		//Client Container
		this.container.updateItemName(nameField);

		//Notify server of name change
		BlockPos pos = this.container.selfPosition;
		ModidPacketHandler.INSTANCE.sendToServer(new GuiBetterAnvilPacket((double) pos.getX(), (double) pos.getY(), (double) pos.getZ(), nameField));
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
		GlStateManager.disableLighting();
		GlStateManager.disableBlend();
		this.nameField.drawTextBox();
	}

	public void updateNameInput() {
		this.nameField.setText(this.container.anvil.getItemNameInput());
	}

	public void setNameInputEnabled(boolean enabled) {
		Keyboard.enableRepeatEvents(enabled);
		this.nameField.setEnabled(enabled);
	}
}