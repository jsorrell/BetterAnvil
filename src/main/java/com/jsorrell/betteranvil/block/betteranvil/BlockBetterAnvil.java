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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class BlockBetterAnvil<TE extends TileEntity> extends BlockTileEntityBase {
	public BlockBetterAnvil(String name) {
		super(Material.ANVIL, name);
		setCreativeTab(CreativeTabs.MISC);
		setHardness(5.0F);
		setResistance(2000.0F);
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileBetterAnvil();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			player.openGui(BetterAnvil.instance, ModGuiHandler.ANVIL, world, pos.getX(), pos.getY(), pos.getZ());
		}
		return true;
	}

	//TODO: Drop items when broken in other slots
	@SuppressWarnings("ConstantConditions")
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileBetterAnvil tile = getTileEntity(world, pos);
		IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);

		//Drop slot 0
		ItemStack stack0 = itemHandler.getStackInSlot(0);
		if (!stack0.isEmpty()) {
			EntityItem item = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack0);
			world.spawnEntity(item);
		}

		//Drop slot 1
		ItemStack stack1 = itemHandler.getStackInSlot(1);
		if (!stack1.isEmpty()) {
			EntityItem item = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack1);
			world.spawnEntity(item);
		}

		super.breakBlock(world, pos, state);
		world.removeTileEntity(pos);
	}

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

	public TileBetterAnvil getTileEntity(IBlockAccess world, BlockPos pos) {
		return (TileBetterAnvil)world.getTileEntity(pos);
	}
}
