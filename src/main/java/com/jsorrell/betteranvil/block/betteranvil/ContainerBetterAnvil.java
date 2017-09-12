package com.jsorrell.betteranvil.block.betteranvil;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

import static java.lang.System.out;

public class ContainerBetterAnvil extends Container {

	private IItemHandler inventory;

	private static final Logger LOGGER = LogManager.getLogger();
	private final World world;
	private final BlockPos selfPosition;
	/** The maximum cost of repairing/renaming in the anvil. */
	public int xpCost;
	/** determined by damage of input item and stackSize of repair materials */
	public int materialCost;
	private String repairedItemName;
	/** The player that has this container open. */
	private final EntityPlayer player;
	private final TileBetterAnvil anvil;

	public ContainerBetterAnvil(InventoryPlayer playerInventory, final World worldIn, final BlockPos blockPosIn, EntityPlayer player)
	{
		this.anvil = (TileBetterAnvil)worldIn.getTileEntity(blockPosIn);
		this.inventory = anvil.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);
		this.selfPosition = blockPosIn;
		this.world = worldIn;
		this.player = player;
		this.addSlotToContainer(new SlotItemHandler(this.inventory, 0, 27, 47){
			@Override
			public void onSlotChanged() {
				ContainerBetterAnvil.this.updateRepairOutput();
				anvil.markDirty();
			}
		});
		this.addSlotToContainer(new SlotItemHandler(this.inventory, 1, 76, 47) {
			@Override
			public void onSlotChanged() {
				ContainerBetterAnvil.this.updateRepairOutput();
				anvil.markDirty();
			}
		});
		this.addSlotToContainer(new SlotItemHandler(this.inventory, 2, 134, 47)
		{
			/**
			 * Prevent inputting into output
			 */
			@Override
			public boolean isItemValid(ItemStack stack)
			{
				return false;
			}
			/**
			 * Return whether this slot's stack can be taken from this slot.
			 */
			@Override
			public boolean canTakeStack(EntityPlayer playerIn)
			{
				return (playerIn.capabilities.isCreativeMode || playerIn.experienceLevel >= ContainerBetterAnvil.this.xpCost) && ContainerBetterAnvil.this.xpCost > 0 && this.getHasStack();
			}
			@Override
			public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack)
			{
				if (!thePlayer.capabilities.isCreativeMode)
				{
					//Fixme: Vanilla experience calculation
					thePlayer.addExperienceLevel(-ContainerBetterAnvil.this.xpCost);
				}

				clearInventorySlot(0);
				clearInventorySlot(1);
				ContainerBetterAnvil.this.xpCost = 0;

				return stack;
			}
		});

		for (int i = 0; i < 3; ++i)
		{
			for (int j = 0; j < 9; ++j)
			{
				this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (int k = 0; k < 9; ++k)
		{
			this.addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 142));
		}
	}


	/**
	 * called when the Anvil Input Slot changes, calculates the new result and puts it in the output slot
	 */
	public void updateRepairOutput()
	{
		ItemStack input0 = this.inventory.getStackInSlot(0);
		this.xpCost = 1;
		int i = 0;
		int repairCost = 0;
		int k = 0;

		if (input0.isEmpty())
		{
			clearInventorySlot(2);
			this.xpCost = 0;
		}
		else
		{
			ItemStack output = input0.copy();
			ItemStack input1 = this.inventory.getStackInSlot(1);
			Map<Enchantment, Integer> enchantmentLevelsMap0 = EnchantmentHelper.getEnchantments(input0);
			repairCost = repairCost + input0.getRepairCost() + (input1.isEmpty() ? 0 : input1.getRepairCost());
			this.materialCost = 0;
			boolean isEnchantedBook = false;

			// 2 Inputs: repairing/combining
			if (!input1.isEmpty())
			{
				//FIXME
				//if (!net.minecraftforge.common.ForgeHooks.onAnvilChange(this, input0, input1, outputSlot, repairedItemName, repairCost)) return;
				isEnchantedBook = input1.getItem() == Items.ENCHANTED_BOOK && !ItemEnchantedBook.getEnchantments(input1).hasNoTags();

				/* Repair input0 with input1 materials */
				if (output.isItemStackDamageable() && output.getItem().getIsRepairable(input0, input1))
				{
					int repairValue = Math.min(output.getItemDamage(), output.getMaxDamage() / 4);

					if (repairValue <= 0)
					{
						clearInventorySlot(0);
						this.xpCost = 0;
						return;
					}

					/* Repeatedly repair */
					int materialCost;

					for (materialCost = 0; repairValue > 0 && materialCost < input1.getCount(); ++materialCost)
					{
						output.setItemDamage(output.getItemDamage() - repairValue);
						++i;
						repairValue = Math.min(output.getItemDamage(), output.getMaxDamage() / 4);
					}

					this.materialCost = materialCost;
				}
				else
				{
					/* Invalid anvil combination */
					if (!isEnchantedBook && (output.getItem() != input1.getItem() || !output.isItemStackDamageable()))
					{
						clearInventorySlot(2);
						this.xpCost = 0;
						return;
					}

					/* Repair two alike items, say two swords */
					if (output.isItemStackDamageable() && !isEnchantedBook)
					{
						int durability0 = input0.getMaxDamage() - input0.getItemDamage();
						int durability1 = input1.getMaxDamage() - input1.getItemDamage();
						// Extra 12% durability
						int newDurability = durability0 + durability1 + output.getMaxDamage() * 12 / 100;
						int newDamage = output.getMaxDamage() - newDurability;

						if (newDamage < 0)
						{
							newDamage = 0;
						}

						if (newDamage < output.getMetadata())
						{
							output.setItemDamage(newDamage);
							i += 2;
						}
					}

					/* Combine enchantments onto tools */
					Map<Enchantment, Integer> enchantmentLevelsMap1 = EnchantmentHelper.getEnchantments(input1);
					boolean hasApplicableEnchantment = false;
					boolean hasUnapplicableEnchantment = false;

					for (Enchantment enchantment1 : enchantmentLevelsMap1.keySet())
					{
						if (enchantment1 != null)
						{
							int enchantmentLevel0 = enchantmentLevelsMap0.getOrDefault(enchantment1, 0);
							int enchantmentLevel1 = enchantmentLevelsMap1.get(enchantment1);
							//If same level, then increment level by one. Else, use highest
							enchantmentLevel1 = enchantmentLevel0 == enchantmentLevel1 ? enchantmentLevel1 + 1 : Math.max(enchantmentLevel1, enchantmentLevel0);
							boolean canApplyEnchantment = enchantment1.canApply(input0);

							if (this.player.capabilities.isCreativeMode || input0.getItem() == Items.ENCHANTED_BOOK)
							{
								canApplyEnchantment = true;
							}

							for (Enchantment enchantment0 : enchantmentLevelsMap0.keySet())
							{
								//Incompatible enchantments
								if (enchantment0 != enchantment1 && !enchantment1.isCompatibleWith(enchantment0))
								{
									canApplyEnchantment = false;
									++i;
								}
							}

							//Update flags
							hasUnapplicableEnchantment = hasUnapplicableEnchantment || !canApplyEnchantment;
							hasApplicableEnchantment = hasApplicableEnchantment || canApplyEnchantment;

							if (canApplyEnchantment)
							{
								if (enchantmentLevel1 > enchantment1.getMaxLevel())
								{
									enchantmentLevel1 = enchantment1.getMaxLevel();
								}

								enchantmentLevelsMap0.put(enchantment1, enchantmentLevel1);
								int enchantment1Rarity = 0;

								switch (enchantment1.getRarity())
								{
									case COMMON:
										enchantment1Rarity = 1;
										break;
									case UNCOMMON:
										enchantment1Rarity = 2;
										break;
									case RARE:
										enchantment1Rarity = 4;
										break;
									case VERY_RARE:
										enchantment1Rarity = 8;
								}

								if (isEnchantedBook)
								{
									enchantment1Rarity = Math.max(1, enchantment1Rarity / 2);
								}

								i += enchantment1Rarity * enchantmentLevel1;

								if (input0.getCount() > 1)
								{
									i = 40;
								}
							}
						}
					}

					if (hasUnapplicableEnchantment && !hasApplicableEnchantment)
					{
						clearInventorySlot(0);
						this.xpCost = 0;
						return;
					}
				}
			}

			/* Rename */
			if (StringUtils.isBlank(this.repairedItemName))
			{
				if (input0.hasDisplayName())
				{
					k = 1;
					i += k;
					output.clearCustomName();
				}
			}
			else if (!this.repairedItemName.equals(input0.getDisplayName()))
			{
				k = 1;
				i += k;
				output.setStackDisplayName(this.repairedItemName);
			}

			if (isEnchantedBook && !output.getItem().isBookEnchantable(output, input1)) output = ItemStack.EMPTY;

			/* Weird logic to calculate xp cost */
			this.xpCost = repairCost + i;

			if (i <= 0)
			{
				output = ItemStack.EMPTY;
			}

			if (k == i && k > 0 && this.xpCost >= 40)
			{
				this.xpCost = 39;
			}

			if (this.xpCost >= 40 && !this.player.capabilities.isCreativeMode)
			{
				output = ItemStack.EMPTY;
			}

			if (!output.isEmpty())
			{
				int k2 = output.getRepairCost();

				if (!input1.isEmpty() && k2 < input1.getRepairCost())
				{
					k2 = input1.getRepairCost();
				}

				if (k != i || k == 0)
				{
					k2 = k2 * 2 + 1;
				}

				output.setRepairCost(k2);
				EnchantmentHelper.setEnchantments(enchantmentLevelsMap0, output);
			}

			clearInventorySlot(2);
			this.inventory.insertItem(2, output, false);
			this.detectAndSendChanges();
		}
	}

	private boolean clearInventorySlot(int slot) {
		ItemStack stack = this.inventory.getStackInSlot(slot);
		int count = stack.getCount();
		if (count > 0) {
			this.inventory.extractItem(slot, count, false);
		}
		return count > 0;
	}

//	public void addListener(IContainerListener listener)
//	{
//		super.addListener(listener);
//		listener.sendWindowProperty(this, 0, this.xpCost);
//	}

//	@SideOnly(Side.CLIENT)
//	public void updateProgressBar(int id, int data)
//	{
//		if (id == 0)
//		{
//			this.xpCost = data;
//		}
//	}

	/**
	 * Called when the container is closed.
	 */
	public void onContainerClosed(EntityPlayer playerIn)
	{
		super.onContainerClosed(playerIn);
		out.println("Closing Container");
		//TODO
	}

	/**
	 * Determines whether supplied player can use this container
	 */
	public boolean canInteractWith(EntityPlayer playerIn)
	{
		return playerIn.getDistanceSq((double)this.selfPosition.getX() + 0.5D, (double)this.selfPosition.getY() + 0.5D, (double)this.selfPosition.getZ() + 0.5D) <= 64.0D;
	}

	/**
	 * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
	 * inventory and the other inventory(s).
	 */
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack0 = slot.getStack();
			itemstack = itemstack0.copy();

			if (index == 2)
			{
				if (!this.mergeItemStack(itemstack0, 3, 39, true))
				{
					return ItemStack.EMPTY;
				}

				slot.onSlotChange(itemstack0, itemstack);
			}
			else if (index != 0 && index != 1)
			{
				if (index >= 3 && index < 39 && !this.mergeItemStack(itemstack0, 0, 2, false))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.mergeItemStack(itemstack0, 3, 39, false))
			{
				return ItemStack.EMPTY;
			}

			if (itemstack0.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}

			if (itemstack0.getCount() == itemstack.getCount())
			{
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, itemstack0);
		}

		return itemstack;
	}

	/**
	 * used by the Anvil GUI to update the Item Name being typed by the player
	 */
//	public void updateItemName(String newName)
//	{
//		this.repairedItemName = newName;
//
//		if (this.getSlot(2).getHasStack())
//		{
//			ItemStack itemstack = this.getSlot(2).getStack();
//
//			if (StringUtils.isBlank(newName))
//			{
//				itemstack.clearCustomName();
//			}
//			else
//			{
//				itemstack.setStackDisplayName(this.repairedItemName);
//			}
//		}
//
//		this.updateRepairOutput();
//	}

}
