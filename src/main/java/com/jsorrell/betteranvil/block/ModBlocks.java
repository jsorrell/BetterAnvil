package com.jsorrell.betteranvil.block;

import com.jsorrell.betteranvil.block.betteranvil.BlockBetterAnvil;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

public class ModBlocks {

	public static BlockBetterAnvil anvil = new BlockBetterAnvil();

	public static void registerBlocks(IForgeRegistry<Block> registry) {
		registry.registerAll(
			anvil
		);
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
