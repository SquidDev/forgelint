package org.squiddev.forgelint;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class CheckInstance {
	private final Context context;
	private final Trees trees;
	private final Elements elements;
	private final Types types;
	private final Names names;

	public CheckInstance(JavacTask task) {
		this.context = ((BasicJavacTask) task).getContext();
		this.trees = Trees.instance(task);
		this.elements = task.getElements();
		this.types = task.getTypes();
		this.names = Names.instance(context);
	}

	public Context context() {
		return context;
	}

	public Trees trees() {
		return trees;
	}

	public Elements elements() {
		return elements;
	}

	public Types types() {
		return types;
	}

	public Names names() {
		return names;
	}
}
