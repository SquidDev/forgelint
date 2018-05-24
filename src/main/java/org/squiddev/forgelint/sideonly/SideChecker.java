package org.squiddev.forgelint.sideonly;

import com.google.auto.service.AutoService;
import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import org.squiddev.forgelint.CheckInstance;
import org.squiddev.forgelint.Checker;

import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;

import static com.sun.tools.javac.tree.JCTree.*;

@AutoService(Checker.class)
public class SideChecker implements Checker {
	@Override
	public void check(CheckInstance instance, CompilationUnitTree root) {
		new SideContextVisitor(instance, root).scan(root, Side.BOTH);
	}

	/**
	 * Infers the current state
	 */
	private static class SideContextVisitor extends TreeScanner<Side, Side> {
		private final CheckInstance instance;
		private final CompilationUnitTree root;
		private final SideProvider sideProvider;
		private final GuardProvider guardProvider;

		public SideContextVisitor(CheckInstance instance, CompilationUnitTree root) {
			this.instance = instance;
			this.sideProvider = new SideProvider(instance);
			this.guardProvider = new GuardProvider(instance);
			this.root = root;
		}

		@Override
		public Side visitClass(ClassTree tree, Side side) {
			Tree superClass = tree.getExtendsClause();
			Side superSide = Side.BOTH;
			if (superClass != null) {
				superSide = sideProvider.getInferredSide(((JCTree) superClass).type.asElement());
				if (!side.compatible(superSide)) {
					instance.trees().printMessage(Diagnostic.Kind.WARNING,
						"Overriding a " + superClass + " method, but we're targeting the " + side + ".",
						superClass, root
					);
				}

				// Prefer parent classes over child ones
				side = side.leftLowest(superSide);
			}

			Side thisSide = sideProvider.getAnnotatedSide(((JCClassDecl) tree).sym);
			if (side == Side.BOTH && superSide != Side.BOTH && thisSide == Side.BOTH) {
				instance.trees().printMessage(Diagnostic.Kind.NOTE,
					"Subclassing a " + superSide + " class, but no @SideOnly annotation",
					superClass, root
				);
			} else if (!superSide.compatible(thisSide)) {
				instance.trees().printMessage(Diagnostic.Kind.WARNING,
					"Subclassing a " + superSide + " class, but have " + thisSide + " annotation",
					superClass, root
				);
			}

			// Prefer an explicit annotation over inherited ones.
			side = thisSide.leftLowest(side);

			side = this.scan(tree.getImplementsClause(), side);
			side = this.scan(tree.getMembers(), side);
			return side;
		}

		@Override
		public Side visitIdentifier(IdentifierTree tree, Side side) {
			JCIdent ident = (JCIdent) tree;
			if (ident.sym != null && !ident.name.toString().equals("super") && !ident.name.toString().equals("this")) {
				Side memberSide = sideProvider.getInferredSide(ident.sym);
				if (!memberSide.higher(side)) {
					instance.trees().printMessage(Diagnostic.Kind.WARNING,
						MessageFormatter.invalidMember(ident.sym, memberSide, side),
						tree, root
					);
				}
			}

			return side;
		}

		@Override
		public Side visitVariable(VariableTree tree, Side side) {
			JCVariableDecl var = (JCVariableDecl) tree;
			if (var.sym.getKind() == ElementKind.FIELD) {
				Side fieldSide = sideProvider.getAnnotatedSide(var.sym);
				if (!side.compatible(fieldSide)) {
					instance.trees().printMessage(Diagnostic.Kind.WARNING,
						"Inside a " + side + " class, but have " + fieldSide + " annotation",
						tree, root
					);
				}

				Side childSide = fieldSide.leftLowest(side);
				scan(tree.getType(), childSide);
				scan(tree.getNameExpression(), childSide);

				// Stripped fields shouldn't initialise anything as the constructor/static initialiser
				// isn't removed, hence an error at runtime.
				if (fieldSide != Side.BOTH && tree.getInitializer() != null) {
					instance.trees().printMessage(Diagnostic.Kind.ERROR,
						"Should not initialise a field annotated with @SideOnly. This should be lazily loaded when required.",
						tree.getInitializer(), root
					);
				}

				// Fields are initialised in the constructor, so we need to use the parent state anyway.
				scan(tree.getInitializer(), side);

				return side;
			} else {
				return super.visitVariable(tree, side);
			}
		}

		@Override
		public Side visitMethod(MethodTree methodTree, Side side) {
			JCMethodDecl method = (JCMethodDecl) methodTree;

			Side overrideSide = sideProvider.getOverrideSide(method.sym);
			if (!side.compatible(overrideSide)) {
				instance.trees().printMessage(Diagnostic.Kind.WARNING,
					"Overriding a " + overrideSide + " method, but we're targeting the " + side + ".",
					methodTree, root
				);
			}

			Side methodSide = sideProvider.getAnnotatedSide(method.sym);
			if (side == Side.BOTH && overrideSide != Side.BOTH && methodSide == Side.BOTH) {
				instance.trees().printMessage(Diagnostic.Kind.NOTE,
					"Overriding a " + overrideSide + " method, but no @SideOnly annotation",
					methodTree, root
				);
			} else if (!overrideSide.compatible(methodSide)) {
				instance.trees().printMessage(Diagnostic.Kind.WARNING,
					"Overriding a " + overrideSide + " method, but have " + methodSide + " annotation",
					methodTree, root
				);
			}

			// Prefer an explicit annotation over inherited ones, and prefer inherited annotations over class ones
			Side innerSide = methodSide.leftLowest(overrideSide.leftLowest(side));
			super.visitMethod(methodTree, innerSide);

			return side;
		}

		@Override
		public Side visitMemberSelect(MemberSelectTree tree, Side side) {
			side = super.visitMemberSelect(tree, side);

			if (tree instanceof JCFieldAccess) {
				JCFieldAccess field = (JCFieldAccess) tree;
				Type type = field.selected.type;
				if (type != null && type.getKind() == TypeKind.DECLARED) {
					Side memberSide = sideProvider.getInferredSide(field.sym);
					if (!memberSide.higher(side)) {
						instance.trees().printMessage(Diagnostic.Kind.WARNING,
							MessageFormatter.invalidMember(field.sym, memberSide, side),
							tree, root
						);
					}
				}
			}

			return side;
		}

		@Override
		public Side visitReturn(ReturnTree returnTree, Side side) {
			super.visitReturn(returnTree, side);
			return Side.NONE;
		}

		@Override
		public Side visitThrow(ThrowTree throwTree, Side side) {
			super.visitThrow(throwTree, side);
			return Side.NONE;
		}

		@Override
		public Side visitIf(IfTree tree, Side side) {
			return visitIf(tree.getCondition(), tree.getThenStatement(), tree.getElseStatement(), side);
		}

		@Override
		public Side visitConditionalExpression(ConditionalExpressionTree tree, Side side) {
			return visitIf(tree.getCondition(), tree.getTrueExpression(), tree.getFalseExpression(), side);
		}

		private Side visitIf(Tree cond, Tree trueB, Tree falseB, Side side) {
			side = scan(cond, side);
			Side condSide = new SideStateVisitor(guardProvider, sideProvider).scan(cond, null);
			if (condSide == null || condSide == Side.NONE) condSide = Side.BOTH;

			if (condSide != Side.BOTH) {
				if (condSide == side) {
					instance.trees().printMessage(Diagnostic.Kind.WARNING,
						"Checking we're on the " + condSide + ", but we already know that.",
						cond, root
					);
				} else if (!condSide.compatible(side)) {
					instance.trees().printMessage(Diagnostic.Kind.WARNING,
						"Checking we're on the " + condSide + ", but we're on the " + side + ".",
						cond, root
					);
				}
			}

			Side left = scan(trueB, condSide.leftLowest(side));
			Side right = scan(falseB, condSide.flip().leftLowest(side));
			Side result = left.highest(right);
			return result == Side.NONE ? side : result;
		}

		@Override
		public Side visitCompilationUnit(CompilationUnitTree tree, Side side) {
			side = side.leftLowest(sideProvider.getInitialSide(tree));
			return super.visitCompilationUnit(tree, side);
		}

		@Override
		public Side scan(Tree tree, Side side) {
			Side newSide = super.scan(tree, side);
			return newSide == null ? side : newSide;
		}

		@Override
		public Side scan(Iterable<? extends Tree> iterable, Side side) {
			if (iterable != null) {
				for (Tree var6 : iterable) {
					side = scan(var6, side);
				}
			}

			return side;
		}
	}

	/**
	 * Attempts to determine whether a condition proves or disproves a given condition.
	 */
	private static class SideStateVisitor extends TreeScanner<Side, Void> {
		private final GuardProvider guard;
		private final SideProvider side;

		private SideStateVisitor(GuardProvider guard, SideProvider side) {
			this.guard = guard;
			this.side = side;
		}

		@Override
		public Side visitConditionalExpression(ConditionalExpressionTree tree, Void obj) {
			Side left = scan(tree.getTrueExpression(), obj);
			Side right = scan(tree.getFalseExpression(), obj);
			return left.highest(right);
		}

		@Override
		public Side visitBinary(BinaryTree tree, Void obj) {
			switch (tree.getKind()) {
				case CONDITIONAL_OR: {
					Side left = scan(tree.getLeftOperand(), obj);
					Side right = scan(tree.getRightOperand(), obj);
					return left.highest(right);
				}
				case EQUAL_TO: {
					Side left = side.getSideLiteral(tree.getLeftOperand());
					Side right = side.getSideLiteral(tree.getRightOperand());
					if (left != Side.NONE && right == Side.NONE) return left;
					if (left == Side.NONE && right != Side.NONE) return right;

					return super.visitBinary(tree, obj);
				}
				case NOT_EQUAL_TO: {
					Side left = side.getSideLiteral(tree.getLeftOperand());
					Side right = side.getSideLiteral(tree.getRightOperand());
					if (left != Side.NONE && right == Side.NONE) return left.flip();
					if (left == Side.NONE && right != Side.NONE) return right.flip();

					return super.visitBinary(tree, obj);
				}
				default:
					return super.visitBinary(tree, obj);
			}
		}

		@Override
		public Side visitUnary(UnaryTree tree, Void obj) {
			Side side = scan(tree.getExpression(), obj);
			switch (tree.getKind()) {
				case LOGICAL_COMPLEMENT:
					return side.flip();
				default:
					return side;
			}
		}

		@Override
		public Side visitMemberSelect(MemberSelectTree tree, Void obj) {
			super.visitMemberSelect(tree, obj);
			return guard.getGuard(tree);
		}

		@Override
		public Side scan(Tree tree, Void obj) {
			Side side = super.scan(tree, obj);
			return side == null ? Side.NONE : side;
		}
	}
}
