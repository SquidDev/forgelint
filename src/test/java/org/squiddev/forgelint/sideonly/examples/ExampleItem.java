package org.squiddev.forgelint.sideonly.examples;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class ExampleItem extends Item {
	@SideOnly(Side.CLIENT)
	private RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if (hasEffect(stack)) {
			return "item." + getUnlocalizedName() + ".effect";
		} else {
			return "item." + getUnlocalizedName() + ".no_effect";
		}
	}

	@Nonnull
	@Override
	@SideOnly(Side.SERVER)
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
		if (world.isRemote) {
			world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, false, player.posX, player.posY, player.posZ, 0, 0, 0);
		}

		return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

	@Override
	public boolean shouldRotateAroundWhenRendering() {
		return true;
	}
}
