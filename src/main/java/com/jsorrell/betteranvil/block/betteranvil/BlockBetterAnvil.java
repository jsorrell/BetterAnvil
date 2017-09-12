package com.jsorrell.betteranvil.block.betteranvil;

import com.jsorrell.betteranvil.BetterAnvil;
import com.jsorrell.betteranvil.ModGuiHandler;
import com.jsorrell.betteranvil.block.BlockTileEntityBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;


public class BlockBetterAnvil extends BlockTileEntityBase<TileBetterAnvil> {
	public BlockBetterAnvil() {
		super(Material.ANVIL, "betteranvil");
		setCreativeTab(CreativeTabs.MISC);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			player.openGui(BetterAnvil.instance, ModGuiHandler.ANVIL, world, pos.getX(), pos.getY(), pos.getZ());
		}
		return true;
	}

	//TODO: Drop items when broken in other slots
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileBetterAnvil tile = getTileEntity(world, pos);
		IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);
		ItemStack stack = itemHandler.getStackInSlot(0);
		if (!stack.isEmpty()) {
			EntityItem item = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack);
			world.spawnEntity(item);
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public Class<TileBetterAnvil> getTileEntityClass() {
		return TileBetterAnvil.class;
	}

	@Nullable
	@Override
	public TileBetterAnvil createTileEntity(World world, IBlockState state) {
		return new TileBetterAnvil();
	}

	@Override
	@Deprecated
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	@Deprecated
	public boolean isFullCube(IBlockState state) {
		return false;
	}
}
