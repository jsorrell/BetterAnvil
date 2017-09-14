package com.jsorrell.betteranvil.block.betteranvil;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class GuiBetterAnvilPacket implements IMessage {
	private String itemName;
	private double x;
	private double y;
	private double z;

	public GuiBetterAnvilPacket() {}

	public GuiBetterAnvilPacket(double x, double y, double z, String itemName) {
		this.itemName = itemName;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.itemName = ByteBufUtils.readUTF8String(buf);
		this.x = buf.readDouble();
		this.y = buf.readDouble();
		this.z = buf.readDouble();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.itemName);
		buf.writeDouble(this.x);
		buf.writeDouble(this.y);
		buf.writeDouble(this.z);
	}

	/* Handles server receiving name from client when changed */
	public static class Handler implements IMessageHandler<GuiBetterAnvilPacket, IMessage> {
		@Override
		public IMessage onMessage(GuiBetterAnvilPacket message, MessageContext ctx) {
			World world = ctx.getServerHandler().player.world;
			TileBetterAnvil anvilTile = (TileBetterAnvil)world.getTileEntity(new BlockPos(message.x, message.y, message.z));

			if (anvilTile == null) {
				throw new RuntimeException();
			}

			if (anvilTile.container != null) {
				anvilTile.container.updateItemName(message.itemName);
			} else {
				anvilTile.setItemNameInput(message.itemName);
			}
			return null;
		}
	}
}