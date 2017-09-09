package com.jsorrell.betteranvil.block;

import com.jsorrell.betteranvil.BetterAnvil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public class BlockBase extends Block {

	protected String name;

	public BlockBase(Material material, String name) {
		super(material);

		this.name = name;

		setUnlocalizedName(name);
		setRegistryName(name);
	}

	public void registerItemBlockModel(Item itemBlock) {
		BetterAnvil.proxy.registerItemRenderer(itemBlock, 0, name);
	}

	public Item createItemBlock() {
		return new ItemBlock(this).setRegistryName(name);
	}

	@Override
	public BlockBase setCreativeTab(CreativeTabs tab) {
		super.setCreativeTab(tab);
		return this;
	}
}
