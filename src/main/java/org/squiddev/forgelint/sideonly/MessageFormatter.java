package org.squiddev.forgelint.sideonly;

import com.sun.tools.javac.code.Symbol;

final class MessageFormatter {
	private MessageFormatter() {
	}

	public static String invalidMember(Symbol symbol, Side expected, Side actual) {
		String kind = symbol.getKind().toString().toLowerCase();

		String suffix;
		switch (actual) {
			case NONE:
				suffix = "we're not on the server or client";
				break;
			case BOTH:
				suffix = "this could be either server or client";
				break;
			default:
				suffix = "we're on the " + actual;
				break;
		}

		return "Using a " + expected + " " + kind + ", but " + suffix;
	}
}
