package monitor.implementation.session;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import monitor.implementation.MonitorRuntimeException;
import monitor.model.Command;
import monitor.model.CommandResult;
import monitor.model.Server;

public class ProcessKiller {

	static Logger logger = Logger.getLogger(ProcessKiller.class.getName());	
	private AllSessionPools allSessionPools = AllSessionPools.getInstance();
	private ChildProcessFinder childProcessFinder = new ChildProcessFinder();

	/** 
	 * Find and then kill all child processes working from the bottom up.
	 * It is only necessary to kill the deepest one in each branch. The effect ripples up the tree. 
	 * Killing never ending processes like tail -F helps the remote server know when it can remove unused ssh sessions.
	 **/
	public CommandResult killSubProcesses(Server server, int parentProcessId, Session controlSession, boolean killParent, String calledBy) {
		CommandResult controlKillResult = CommandResult.DEFAULT;
		boolean controlSessionPassedIn = true;
		try {
			if (controlSession == null) {
				controlSessionPassedIn = false;
				logger.info(String.format("Starting control session to kill subprocesses of PID %d on %s. Called by %s", parentProcessId, server, calledBy));
				controlSession = allSessionPools.getServerSessionPool(server).getControlSession(calledBy + "->ProcessKiller.killSubProcesses");
			}
			ArrayList<Integer> bottomUpProcessTree = getChildren(parentProcessId, controlSession);
			if (killParent) {
				bottomUpProcessTree.add(parentProcessId);
			}
			if (bottomUpProcessTree.size() > 0) {
				// sudo is needed because the command may have done a su. 
				// Note, if sudoers file has "%admin ALL=(ALL)" you can just "sudo usermod -Gadmin auto"
				// TODO - rather than requiring auto to be in sudoers check for "bash: kill: (8865) - Operation not permitted" first ?
				StringBuilder sb = new StringBuilder("sudo kill"); 
				for (int proc : bottomUpProcessTree) {
					sb.append(" ").append(proc);
				}
				controlKillResult = controlSession.executeCommand(new Command(sb.toString()));
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, String.format("Problem killing subprocesses of PID %d on %s.", parentProcessId, server), e);
		} finally {
			if (!controlSessionPassedIn && controlSession != null && controlSession.isLoggedOn()) {
				// if we close the control session, it will start a SessionClosingRunnable, which calls
				// this method which either creates or finds a control session which we close here.
				// This creates an endless loop of creating and destroying processes. All done with the same
				// ssh login. Currently, they are created and destroyed at the same rate.
				// Perhaps some flag hiding in another class with a strange name will exist to stop this.?
				controlSession.setKillSubprocesesWhenFinished(false);
				controlSession.close(calledBy + "->ProcessKiller.killSubProcesses"); // killing sub-processes would require another control session
			}
		}
		return controlKillResult;
	}	
	
	public CommandResult killSubProcesses(Server server, int parentProcessId, String calledBy) {
		// todo needs a redesign
		if (calledBy.contains("SessionClosingRunnable.close->ProcessKiller.killSubProcesses->SessionClosingRunnable.close")) {
			try {
				throw new MonitorRuntimeException("loop detected");
			} catch (Exception e) {
				logger.log(Level.SEVERE, "logging but not re-throwing", e);
			}
		}
		return killSubProcesses(server, parentProcessId, null, false, calledBy);
	}

	public CommandResult killProcessAndSubProcesses(Server server, int parentProcessId, Session controlSession, String calledBy) {
		return killSubProcesses(server, parentProcessId, controlSession, true, calledBy);
	}
	
	/** @return The process tree flattened to a list with deepest nodes first. */
	public ArrayList<Integer> getChildren(int parentProcessId, Session controlSession) {
		return reverse(getChildrenTopDownOrder(parentProcessId, controlSession));
	}
	
	ArrayList<Integer> getChildrenTopDownOrder(int parentProcessId, Session controlSession) {
		ArrayList<Integer> allChildren = new ArrayList<Integer>();
		ArrayList<Integer> thisLevelChildren = childProcessFinder.getChildren(parentProcessId, controlSession);
		allChildren.addAll(thisLevelChildren);
		for (int thisLevelchild : thisLevelChildren) {
			ArrayList<Integer> nextGeneration = getChildrenTopDownOrder(thisLevelchild, controlSession);
			allChildren.addAll(nextGeneration);
		}
		return allChildren;
	}

	private ArrayList<Integer> reverse(ArrayList<Integer> allChildren) {
		ArrayList<Integer> reversed = new ArrayList<Integer>();
		for (int x = allChildren.size() - 1; x > -1; x--) {
			reversed.add(allChildren.get(x));
		}
		return reversed;
	}

	public void setChildProcessFinder(ChildProcessFinder childProcessFinder) {
		this.childProcessFinder = childProcessFinder;
	}

	
}
