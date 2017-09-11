package org.squiddev.forgelint.sideonly.basic;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class MethodWrongArguments {
	/**
	 * warning: Referencing a CLIENT identifier, but in a BOTH context
	 */
	public void clientOnly(TileEntitySpecialRenderer<?> renderer) {
	}
}
