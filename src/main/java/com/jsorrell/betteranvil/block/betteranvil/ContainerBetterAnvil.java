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

import javax.annotation.Nonnull;
import java.util.Map;

public class ContainerBetterAnvil extends Container {
	private IItemHandler inputSlots;
	private final World world;
	final BlockPos selfPosition;
	/** The maximum cost of repairing/renaming in the anvil. */
	int xpCost;
	/** determined by damage of input item and stackSize of repair materials */
	private int materialCost;
	/** The player that has this container open. */
	private final EntityPlayer player;
	public final TileBetterAnvil anvil;
	public GuiBetterAnvil gui = null; //null if on server side or gui not initialized
	private final IInventory outputSlot;

	public ContainerBetterAnvil(InventoryPlayer playerInventory, final World worldIn, final BlockPos blockPosIn, EntityPlayer player)
	{
		this.anvil = (TileBetterAnvil)worldIn.getTileEntity(blockPosIn);
		this.inputSlots = anvil.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);
		this.outputSlot = new InventoryCraftResult();
		this.selfPosition = blockPosIn;
		this.world = worldIn;
		this.player = player;
		this.anvil.container = this; //TODO: should this be here?
		this.addSlotToContainer(new SlotItemHandler(this.inputSlots, 0, 27, 47){
			@Override
			public void putStack(@Nonnull ItemStack stack) {
				if (!stack.isItemEqual(ContainerBetterAnvil.this.inputSlots.getStackInSlot(0)) && !stack.getUnlocalizedName().equals("tile.air")) {
					anvil.setItemNameInput(stack.getDisplayName());
					if (gui != null) {
						gui.updateNameInput();
						gui.setNameInputEnabled(true);
					}
				}
				super.putStack(stack);

				updateRepairOutput();
				anvil.markDirty();
			}

			@Override
			public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {
				super.onTake(thePlayer, stack);
				anvil.setItemNameInput("");
				if (gui != null) {
					gui.updateNameInput();
					gui.setNameInputEnabled(false);
				}
				updateRepairOutput();
				anvil.markDirty();
				return stack;
			}
		});
		this.addSlotToContainer(new SlotItemHandler(this.inputSlots, 1, 76, 47) {
			@Override
			public void onSlotChanged() {
				updateRepairOutput();
				anvil.markDirty();
			}
		});
		this.addSlotToContainer(new Slot(this.outputSlot, 0, 134, 47)
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
				ContainerBetterAnvil.this.inputSlots.extractItem(1, ContainerBetterAnvil.this.materialCost, false);
				ContainerBetterAnvil.this.xpCost = 0;

				if(!world.isRemote) {
					worldIn.playEvent(1030, blockPosIn, 0);
				}

				anvil.setItemNameInput("");
				anvil.markDirty();

				if (gui != null) {
					gui.updateNameInput();
					gui.setNameInputEnabled(false);
				}
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

		this.updateRepairOutput();
	}

	/**
	 * called when the Anvil Input Slot changes, calculates the new result and puts it in the output slot
	 */
	public void updateRepairOutput()
	{
		ItemStack input0 = this.inputSlots.getStackInSlot(0);
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
			ItemStack input1 = this.inputSlots.getStackInSlot(1);
			Map<Enchantment, Integer> enchantmentLevelsMap0 = EnchantmentHelper.getEnchantments(input0);
			repairCost = repairCost + input0.getRepairCost() + (input1.isEmpty() ? 0 : input1.getRepairCost());
			this.materialCost = 0;
			boolean isEnchantedBook = false;

			// 2 Inputs: repairing/combining
			if (!input1.isEmpty())
			{
				IInventory onAnvilChangeOutput = new InventoryCraftResult();
				//TODO: add tests for this
				ContainerRepair onAnvilChangeContainer = new ContainerRepair(null, null, null);
				//FIXME: this will prob cause errors + hacky AF
//				ContainerRepair onAnvilChangeContainer = (ContainerRepair)(Object)new Object() {
//					public int maximumCost;
//					public int materialCost;
//				};
				if (!net.minecraftforge.common.ForgeHooks.onAnvilChange(onAnvilChangeContainer, input0, input1, onAnvilChangeOutput, this.anvil.getItemNameInput(), repairCost)) {
					if (!onAnvilChangeOutput.isEmpty()) {
						this.xpCost = onAnvilChangeContainer.maximumCost;
						this.materialCost = onAnvilChangeContainer.materialCost;
						this.clearInventorySlot(2);
						this.outputSlot.setInventorySlotContents(0, onAnvilChangeOutput.getStackInSlot(0));
					}
				}
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
			if (StringUtils.isBlank(this.anvil.getItemNameInput()))
			{
				if (input0.hasDisplayName())
				{
					k = 1;
					i += k;
					output.clearCustomName();
				}
			}
			else if (!this.anvil.getItemNameInput().equals(input0.getDisplayName()))
			{
				k = 1;
				i += k;
				output.setStackDisplayName(this.anvil.getItemNameInput());
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
			this.outputSlot.setInventorySlotContents(0, output);
		}
	}

	private boolean clearInventorySlot(int slot) {
		int count;
		if (slot < 2) {
			ItemStack stack = this.inputSlots.getStackInSlot(slot);
			count = stack.getCount();
			if (count > 0) {
				this.inputSlots.extractItem(slot, count, false);
			}
		} else {
			ItemStack stack = this.outputSlot.getStackInSlot(0);
			count = stack.getCount();
			if (count > 0) {
				this.outputSlot.clear();
			}
		}
		return count > 0;
	}

	public void addListener(IContainerListener listener)
	{
		super.addListener(listener);
		listener.sendWindowProperty(this, 0, this.xpCost);
	}

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
		this.anvil.container = null;
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
	 * inputSlots and the other inputSlots(s).
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
	 * used by the BetterAnvil GUI to update the Item Name being typed by the player
	 */
	public void updateItemName(String newName)
	{
		this.anvil.setItemNameInput(newName);

		if (this.getSlot(2).getHasStack())
		{
			ItemStack itemstack = this.getSlot(2).getStack();

			if (StringUtils.isBlank(newName))
			{
				itemstack.clearCustomName();
			}
			else
			{
				itemstack.setStackDisplayName(newName);
			}
		}

		this.updateRepairOutput();
	}
}
