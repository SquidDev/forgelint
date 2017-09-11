# forgelint
Primitive static analysis for Forge mods

## Features
 - Warn when using client-/server-only functionality, when not annotated with `@SideOnly`.

## Example
```java
public class ExampleItem extends Item {
	@SideOnly(Side.CLIENT)
	private RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
	//                              ^
	// warning: Using a CLIENT class, but this could be either server or client

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if (hasEffect(stack)) {
		//  ^
		// warning: Using a CLIENT method, but this could be either server or client
			return "item." + getUnlocalizedName() + ".effect";
		} else {
			return "item." + getUnlocalizedName() + ".no_effect";
		}
	}
	
	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
		if (world.isRemote) {
			world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, false, player.posX, player.posY, player.posZ, 0, 0, 0);
			// No warning: world.isRemote acts as a guard
		}

		return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}
	
	@Override
	public boolean shouldRotateAroundWhenRendering() {
		//         ^
		// Note: Overriding a CLIENT method, but no @SideOnly annotation
		return true;
	}
}
```

## Usage
Add this to your `build.gradle`:

```groovy
repositories { maven { url 'https://dl.bintray.com/squiddev/maven' } }
dependencies { provided "org.squiddev:forgelint:0.1.0" }
compileJava { options.compilerArgs += ["-Xplugin:ForgeLint"] }
```

## Planned features
 - Detect non-server-thread manipulation of the world.
 - Smarter control flow handling, meaning we don't require `@SideOnly` on private methods. 

## See also
The [MinecraftDev](https://github.com/minecraft-dev/MinecraftDev) plugin also provides a sidedness checker. The plugin
is slightly more lenient than ForgeLint in some of its warnings, but is integrated into the editor which provides a 
nicer experience.
