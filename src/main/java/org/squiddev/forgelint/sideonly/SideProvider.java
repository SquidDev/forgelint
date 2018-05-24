package org.squiddev.forgelint.sideonly;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.tools.javac.util.Names;
import org.squiddev.forgelint.CheckInstance;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.HashMap;
import java.util.Map;

import static com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import static com.sun.tools.javac.tree.JCTree.JCIdent;

class SideProvider {
	private static final String SIDE_ONLY_NAME = "net.minecraftforge.fml.relauncher.SideOnly";
	private static final String SIDE_NAME = "net.minecraftforge.fml.relauncher.Side";

	private final Types types;
	private final Names names;
	private final Elements elements;

	private final TypeMirror sideOnly;
	private final TypeMirror side;

	private final HashMap<Element, Side> sides = new HashMap<>();

	public SideProvider(CheckInstance instance) {
		this.types = instance.types();
		this.names = instance.names();
		this.elements = instance.elements();

		this.sideOnly = instance.elements().getTypeElement(SIDE_ONLY_NAME).asType();
		this.side = instance.elements().getTypeElement(SIDE_NAME).asType();


	}

	public Side getSideLiteral(ExpressionTree tree) {
		// Detect Side.CLIENT and Side.SERVER
		if (tree instanceof MemberSelectTree) {
			JCFieldAccess member = (JCFieldAccess) tree;
			if (member.selected instanceof IdentifierTree) {
				JCIdent ident = (JCIdent) member.selected;
				if (ident.sym.toString().equals(SIDE_NAME) && types.isSameType(member.type, side)) {
					return Side.valueOf(member.name.toString());
				}
			}
		}

		return Side.NONE;
	}

	public Side getInferredSide(Element element) {
		if (element == null) return Side.BOTH;

		Side side = sides.get(element);
		if (side == null) {
			sides.put(element, side = getSideImpl(element));
		}
		return side;
	}

	public Side getAnnotatedSide(Element element) {
		if (element == null) return Side.BOTH;

		for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
			if (types.isSameType(mirror.getAnnotationType(), sideOnly)) {
				for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> pair : mirror.getElementValues().entrySet()) {
					if (pair.getKey().getSimpleName().equals(names.fromString("value"))) {
						return Side.valueOf(pair.getValue().getValue().toString());
					}
				}
			}
		}

		return Side.BOTH;
	}

	public Side getOverrideSide(ExecutableElement element) {
		// Attempt to find the overriding method
		if (element.getEnclosingElement() instanceof TypeElement) {
			TypeElement parent = (TypeElement) element.getEnclosingElement();

			Side side = getOverrideSide(parent, element, parent.getSuperclass());
			if (side != Side.BOTH) return side;

			for (TypeMirror mirror : parent.getInterfaces()) {
				side = getOverrideSide(parent, element, mirror);
				if (side != Side.BOTH) return side;
			}
		}

		return Side.BOTH;
	}

	private Side getOverrideSide(TypeElement parent, ExecutableElement element, TypeMirror mirror) {
		Element interfaceElem = types.asElement(mirror);
		if (interfaceElem instanceof TypeElement) {
			for (Element child : elements.getAllMembers((TypeElement) interfaceElem)) {
				if (child instanceof ExecutableElement && elements.overrides(element, (ExecutableElement) child, parent)) {
					return getInferredSide(child);
				}
			}
		}

		return Side.BOTH;
	}

	private Side getSideImpl(Element element) {
		// Directly annotated
		Side side = getAnnotatedSide(element);
		if (side != Side.BOTH) return side;

		// Parent classes/methods
		Element enclosing = element.getEnclosingElement();
		if (enclosing instanceof TypeElement) {
			side = getInferredSide(enclosing);
			if (side != Side.BOTH) return side;
		}

		// Super class
		if (element instanceof TypeElement) {
			side = getInferredSide(types.asElement(((TypeElement) element).getSuperclass()));
			if (side != Side.BOTH) return side;
		}

		return Side.BOTH;
	}

	public Side getInitialSide(CompilationUnitTree tree) {
		// Some awful heuristics to determine what package we're in.
		String name = tree.getPackageName().toString();
		if (name.endsWith(".client") || name.contains(".client.")) return Side.CLIENT;

		return Side.BOTH;
	}
}
