package org.squiddev.forgelint.sideonly.basic;

import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ConditionPointless {
	/**
	 * warning: Checking for a CLIENT context, but already in one!
	 */
	@SideOnly(Side.CLIENT)
	public void clientOnly(World world) {
		if (world.isRemote) {

		}
	}
}
