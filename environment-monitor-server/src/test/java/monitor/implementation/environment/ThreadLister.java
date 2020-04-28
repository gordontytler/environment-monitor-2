package monitor.implementation.environment;


/**
 * This is for performance testing. It seems that the UNIXProcess.waitForProcessExit starts a 
 * "process reaper" thread that uses most of the CPU.
 * 
 * Copied from
 * http://www.java-forums.org/java-lang/7345-listing-all-threads-threadgroups-vm.html
 */

public class ThreadLister {
	
	
	private void printThreadInfo(Thread t, String indent) {
		if (t == null)
			return;
		System.out.println(indent + "Thread: " + t.getName() + "  Priority: " + t.getPriority()
				+ (t.isDaemon() ? " Daemon" : "") + (t.isAlive() ? "" : " Not Alive"));
	}

	/** Display info about a thread group */
	private void printGroupInfo(ThreadGroup g, String indent) {
		if (g == null)
			return;
		int numThreads = g.activeCount();
		int numGroups = g.activeGroupCount();
		Thread[] threads = new Thread[numThreads];
		ThreadGroup[] groups = new ThreadGroup[numGroups];

		g.enumerate(threads, false);
		g.enumerate(groups, false);

		System.out.println(indent + "Thread Group: " + g.getName() + "  Max Priority: " + g.getMaxPriority()
				+ (g.isDaemon() ? " Daemon" : ""));

		for (int i = 0; i < numThreads; i++)
			printThreadInfo(threads[i], indent + "    ");
		for (int i = 0; i < numGroups; i++)
			printGroupInfo(groups[i], indent + "    ");
	}

	/** Find the root thread group and list it recursively */
	public void listAllThreads() {
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

		printGroupInfo(rootThreadGroup, "");
	}


}