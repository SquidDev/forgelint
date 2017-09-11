package org.squiddev.forgelint.sideonly.basic;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

/**
 * warning: Subclassing a CLIENT class, but no @SideOnly annotation
 */
public class ClassMissingAnnotation extends TileEntitySpecialRenderer {
}
