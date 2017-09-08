package com.jsorrell.betteranvil;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

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

	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent e) {

	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent e) {

	}
}
