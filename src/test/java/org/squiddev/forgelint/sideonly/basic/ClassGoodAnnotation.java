package org.squiddev.forgelint.sideonly.basic;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * OK
 */
@SideOnly(Side.CLIENT)
public class ClassGoodAnnotation extends TileEntitySpecialRenderer {
}
