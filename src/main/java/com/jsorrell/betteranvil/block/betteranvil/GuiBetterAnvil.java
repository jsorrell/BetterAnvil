package com.jsorrell.betteranvil.block.betteranvil;

import com.jsorrell.betteranvil.block.ModBlocks;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public class GuiBetterAnvil extends GuiContainer {
	private InventoryPlayer playerInv;
	//TODO: replace with custom resource?
	private static final ResourceLocation BG_TEXTURE = new ResourceLocation("textures/gui/container/anvil.png");

	public GuiBetterAnvil(Container container, InventoryPlayer playerInv) {
		super(container);
		this.playerInv = playerInv;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1, 1, 1, 1);
		mc.getTextureManager().bindTexture(BG_TEXTURE);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String name = I18n.format(ModBlocks.anvil.getUnlocalizedName() + ".name");
		fontRenderer.drawString(name, xSize / 2 - fontRenderer.getStringWidth(name) / 2, 6, 0x404040);
		fontRenderer.drawString(playerInv.getDisplayName().getUnformattedText(), 8, ySize - 94, 0x404040);
	}

	@Override
	protected void renderHoveredToolTip(int p_191948_1_, int p_191948_2_) {
		super.renderHoveredToolTip(p_191948_1_, p_191948_2_);
		System.out.println("tooltip rendered");
	}


}
