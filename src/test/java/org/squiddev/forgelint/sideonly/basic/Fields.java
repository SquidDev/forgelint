package org.squiddev.forgelint.sideonly.basic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Fields {
	/**
	 * OK
	 */
	@SideOnly(Side.CLIENT)
	public TileEntitySpecialRenderer<?> acceptable;

	/**
	 * warning: Using a CLIENT class, but this could be either server or client
	 */
	public TileEntitySpecialRenderer<?> dungeons;

	/**
	 * warning: Using a CLIENT class, but this could be either server or client
	 * warning: Using a CLIENT method, but this could be either server or client
	 */
	@SideOnly(Side.CLIENT)
	public Minecraft tenMillionYears = null;
}
