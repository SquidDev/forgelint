package org.squiddev.forgelint.sideonly.basic;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * warning: Subclassing a CLIENT class, but have SERVER annotation
 */
@SideOnly(Side.SERVER)
public class ClassWrongAnnotation extends TileEntitySpecialRenderer {
}
