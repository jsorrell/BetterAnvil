package com.jsorrell.betteranvil.block;

import com.jsorrell.betteranvil.block.betteranvil.BlockBetterAnvil;
import com.jsorrell.betteranvil.block.betteranvil.TileBetterAnvil;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class ModBlocks {

	public static BlockBetterAnvil<TileBetterAnvil> anvil = new BlockBetterAnvil("betteranvil");

	public static void registerBlocks(IForgeRegistry<Block> registry) {
		registry.registerAll(
			anvil
		);
		GameRegistry.registerTileEntity(anvil.getTileEntityClass(), anvil.getRegistryName().toString());
	}

	public static void registerItemBlocks(IForgeRegistry<Item> registry) {
		registry.registerAll(
			anvil.createItemBlock()
		);
	}

	public static void registerItemBlockModels() {
		anvil.registerItemBlockModel(Item.getItemFromBlock(anvil));
	}
}
