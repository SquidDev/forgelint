package org.squiddev.forgelint.sideonly;

enum Side {
	/**
	 * The top type. Represents a system which must support both sides.
	 */
	BOTH(2),

	/**
	 * Equivalent to {@link net.minecraftforge.fml.relauncher.Side#SERVER}.
	 */
	SERVER(1),

	/**
	 * Equivalent to {@link net.minecraftforge.fml.relauncher.Side#CLIENT}.
	 */
	CLIENT(1),

	/**
	 * The bottom type. Represents a system which supports neither side.
	 *
	 * This is used for expressions which will not continue execution, such as {@code throw} or {@code return}.
	 */
	NONE(0);

	private final int level;

	Side(int level) {
		this.level = level;
	}

	public boolean higher(Side other) {
		return this.level > other.level || this == other;
	}

	public boolean compatible(Side other) {
		return this.level != other.level || this == other;
	}

	/**
	 * Find the lowest possible subtype, preferring the left in case of a conflict.
	 *
	 * @param other The other side to unify
	 * @return The unified sides.
	 */
	public Side leftLowest(Side other) {
		if (this == other) return this;
		return this.level <= other.level ? this : other;
	}

	/**
	 * Find the highest
	 *
	 * @param other The other side to unify.
	 * @return The unified side.
	 */
	public Side highest(Side other) {
		if (this == other) return this;
		if (this.level == other.level) return NONE;
		return this.level < other.level ? other : this;
	}

	public Side flip() {
		switch (this) {
			case CLIENT:
				return Side.SERVER;
			case SERVER:
				return Side.CLIENT;
			default:
				return this;
		}
	}
}
