package monitor.implementation.shell;

public class ProcessReaperTweaker {

	
	private void checkThread(Thread t, String indent) {
		if (t == null)
			return;
		if ("process reaper".equals(t.getName()) && t.getPriority() == 5) {
			t.setPriority(Thread.MIN_PRIORITY);
		}
	}

	/** Display info about a thread group */
	private void checkThreadGroup(ThreadGroup g, String indent) {
		if (g == null)
			return;
		int numThreads = g.activeCount();
		int numGroups = g.activeGroupCount();
		Thread[] threads = new Thread[numThreads];
		ThreadGroup[] groups = new ThreadGroup[numGroups];

		g.enumerate(threads, false);
		g.enumerate(groups, false);

		for (int i = 0; i < numThreads; i++)
			checkThread(threads[i], indent + "    ");
		for (int i = 0; i < numGroups; i++)
			checkThreadGroup(groups[i], indent + "    ");
	}

	/** Find the root thread group and list it recursively */
	public void reduceThreadPriority() {
		ThreadGroup currentThreadGroup;
		ThreadGroup rootThreadGroup;
		ThreadGroup parent;

		// Get the current thread group
		currentThreadGroup = Thread.currentThread().getThreadGroup();

		// Now go find the root thread group
		rootThreadGroup = currentThreadGroup;
		parent = rootThreadGroup.getParent();
		while (parent != null) {
			rootThreadGroup = parent;
			parent = parent.getParent();
		}

		checkThreadGroup(rootThreadGroup, "");
	}
}	
