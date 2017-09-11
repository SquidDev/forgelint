package org.squiddev.forgelint.sideonly.basic;

import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ConditionConflicting {
	/**
	 * warning: Checking for a SERVER context, but in a CLIENT one.
	 */
	@SideOnly(Side.CLIENT)
	public void clientOnly(World world) {
		if (!world.isRemote) {

		}
	}
}
