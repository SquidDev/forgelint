package org.squiddev.forgelint;

import com.sun.source.tree.CompilationUnitTree;

public interface Checker {
	void check(CheckInstance instance, CompilationUnitTree tree);
}
