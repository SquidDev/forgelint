package org.squiddev.forgelint.sideonly;

import com.sun.source.tree.MemberSelectTree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import org.squiddev.forgelint.CheckInstance;

import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

class GuardProvider {
	private static final String WORLD_NAME = "net.minecraft.world.World";

	private final Types types;

	private final TypeMirror typeWorld;

	public GuardProvider(CheckInstance instance) {
		this.types = instance.types();

		this.typeWorld = instance.elements().getTypeElement(WORLD_NAME).asType();
	}

	public Side getGuard(MemberSelectTree tree) {
		JCFieldAccess field = (JCFieldAccess) tree;
		Type type = field.selected.type;
		if (type != null) {
			if (field.sym.getKind() == ElementKind.FIELD && types.isSameType(type, typeWorld) && field.name.toString().equals("isRemote")) {
				return Side.CLIENT;
			}
		}

		return Side.NONE;
	}
}
