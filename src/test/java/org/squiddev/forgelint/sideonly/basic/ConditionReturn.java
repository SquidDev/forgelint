package org.squiddev.forgelint.sideonly.basic;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ConditionReturn {
	/**
	 * OK
	 */
	public void methodClient(World world) {
		if (!world.isRemote) return;

		Minecraft.getMinecraft();
	}

	/**
	 * warning: Referencing a CLIENT member, but in a SERVER context
	 * warning: Referencing a CLIENT identifier, but in a SERVER context
	 */
	public void methodServer(World world) {
		if (world.isRemote) return;

		Minecraft.getMinecraft();
	}

	/**
	 * OK
	 */
	public void methodClient(MessageContext context) {
		if (context.side != Side.CLIENT) return;

		Minecraft.getMinecraft();
	}
}
