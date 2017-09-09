package com.jsorrell.betteranvil.block.betteranvil;

import com.jsorrell.betteranvil.block.BlockTileEntityBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nullable;


public class BlockBetterAnvil extends BlockTileEntityBase<TileEntityBetterAnvil> {
	public BlockBetterAnvil() {
		super(Material.ANVIL, "betteranvil");
		setCreativeTab(CreativeTabs.MISC);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			TileEntityBetterAnvil tile = getTileEntity(world, pos);
			player.sendStatusMessage(new TextComponentString(TileEntityBetterAnvil.TILEENTITYMESSAGE), true);
		}
		return true;
	}

	@Override
	public Class<TileEntityBetterAnvil> getTileEntityClass() {
		return TileEntityBetterAnvil.class;
	}

	@Nullable
	@Override
	public TileEntityBetterAnvil createTileEntity(World world, IBlockState state) {
		return new TileEntityBetterAnvil();
	}
}
