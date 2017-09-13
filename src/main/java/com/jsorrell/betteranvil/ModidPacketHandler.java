package com.jsorrell.betteranvil;

import com.jsorrell.betteranvil.block.betteranvil.GuiBetterAnvilPacket;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ModidPacketHandler {
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(BetterAnvil.MODID);
	private static int discriminator = 0;

	public static void registerPacketHandlers() {
		INSTANCE.registerMessage(GuiBetterAnvilPacket.Handler.class, GuiBetterAnvilPacket.class, discriminator++, Side.SERVER);
	}
}