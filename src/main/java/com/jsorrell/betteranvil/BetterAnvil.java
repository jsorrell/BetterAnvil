package com.jsorrell.betteranvil;

import com.jsorrell.betteranvil.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = BetterAnvil.MODID, name = BetterAnvil.NAME, version=BetterAnvil.VERSION)
public class BetterAnvil {
	public static final String MODID = "betteranvil";
	public static final String NAME = "Better Anvil";
	public static final String VERSION = "1.0.0";

	@SidedProxy(serverSide = "com.jsorrell.betteranvil.CommonProxy", clientSide = "com.jsorrell.betteranvil.ClientProxy")
	public static CommonProxy proxy;

	@Mod.Instance
	public static BetterAnvil instance;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new ModGuiHandler());
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent e) {
		ModidPacketHandler.registerPacketHandlers();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent e) {
	}

	@Mod.EventBusSubscriber
	public static class RegistrationHandler {
		@SubscribeEvent
		public static void registerItems(RegistryEvent.Register<Item> event) {
			ModBlocks.registerItemBlocks(event.getRegistry());
		}

		@SubscribeEvent
		public static void registerBlocks(RegistryEvent.Register<Block> event) {
			ModBlocks.registerBlocks(event.getRegistry());
		}

		@SubscribeEvent
		public static void registerModels(ModelRegistryEvent event) {
			ModBlocks.registerItemBlockModels();
		}
	}
}
