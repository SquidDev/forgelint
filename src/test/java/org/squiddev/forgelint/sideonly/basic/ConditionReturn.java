package org.squiddev.forgelint.sideonly.basic;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

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
}
