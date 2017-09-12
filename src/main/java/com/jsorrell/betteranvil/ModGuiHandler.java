package com.jsorrell.betteranvil;

import com.jsorrell.betteranvil.block.betteranvil.ContainerBetterAnvil;
import com.jsorrell.betteranvil.block.betteranvil.GuiBetterAnvil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModGuiHandler implements IGuiHandler {
	public static final int ANVIL = 0;

	@Override
	public Container getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID) {
			case ANVIL:
				return new ContainerBetterAnvil(player.inventory, world, new BlockPos(x, y, z), player);
			default:
				return null;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID) {
			case ANVIL:
				return new GuiBetterAnvil(getServerGuiElement(ID, player, world, x, y, z), player.inventory);
			default:
				return null;
		}
	}
}


