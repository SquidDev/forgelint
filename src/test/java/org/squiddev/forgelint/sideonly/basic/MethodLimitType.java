package org.squiddev.forgelint.sideonly.basic;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MethodLimitType {
	/**
	 * OK
	 */
	@SideOnly(Side.CLIENT)
	public void clientOnly() {
	}
}
