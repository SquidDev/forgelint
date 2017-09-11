package org.squiddev.forgelint;

import com.google.auto.service.AutoService;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import org.squiddev.forgelint.sideonly.SideChecker;

@AutoService(Plugin.class)
public class ForgeLintPlugin implements Plugin {
	@Override
	public String getName() {
		return "ForgeLint";
	}

	@Override
	public void init(JavacTask task, String... args) {
		CheckInstance instance = new CheckInstance(task);
		task.addTaskListener(new TaskListener() {
			@Override
			public void started(TaskEvent event) {
			}

			@Override
			public void finished(TaskEvent event) {
				if (event.getKind() != TaskEvent.Kind.ANALYZE) return;
				new SideChecker().check(instance, event.getCompilationUnit());
			}
		});
	}
}
