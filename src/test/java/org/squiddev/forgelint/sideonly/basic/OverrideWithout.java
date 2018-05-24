package org.squiddev.forgelint.sideonly.basic;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

/**
 * This used to warn as {@link net.minecraft.util.registry.IRegistry#getObject(Object)}
 * is client only, but Forge's variant is not.
 */
public class OverrideWithout {
	public Item getItem(ResourceLocation name) {
		return Item.REGISTRY.getObject(name);
	}
}
