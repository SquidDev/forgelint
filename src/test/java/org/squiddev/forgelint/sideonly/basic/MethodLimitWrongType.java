package org.squiddev.forgelint.sideonly.basic;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public class MethodLimitWrongType {
	/**
	 * warning: Inside a SERVER class, but have CLIENT annotation
	 */
	@SideOnly(Side.CLIENT)
	public void clientOnly() {
	}
}
