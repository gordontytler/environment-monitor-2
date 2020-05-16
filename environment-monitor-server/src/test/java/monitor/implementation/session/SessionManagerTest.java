package monitor.implementation.session;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;


public class SessionManagerTest {

	SessionManager sessionManager;

	@After
	public void tearDown() {
		sessionManager.setKeepRunning(false);
		sessionManager.interruptThreads();
	}
	
	// Test the real real-passwords-in-config.properties and logging
	public static void main(String[] args) throws InterruptedException {
		SessionManager sessionManager = new SessionManager();
		sessionManager.setLogFine(false);
		new Thread(sessionManager, "SessionManager").start();
	}
	
	@Test
	public void testStuckThreads() throws InterruptedException {
		
		//Thread.sleep(20000); // let things settle down from other tests
		System.out.println("\n\nStart of SessionManagerTest\n\n");
		
		sessionManager = new SessionManager();
		sessionManager.setDelayBetweenRuns(50);
		sessionManager.setLogFine(true);
		Thread sessionManagerThread = new Thread(sessionManager, "SessionManager");
		long delayBeforeRunning = 0;
		for (SessionManagementRunnable runnable : sessionManager.getRunnables()) {
			runnable.setDelayBeforeRunning(delayBeforeRunning);
			delayBeforeRunning += 150;
			runnable.setDelayBetweenRuns(100);
			runnable.setMaxExpectedDelayBetweenRuns(500);
		}
		sessionManagerThread.start();
		
		// wait until all the threads have run 
		waitFor("more than", 4, 1000);
		
		// now aquire the lock to halt the threads
		System.out.println("About to get sessionManagementLock at " + System.currentTimeMillis());
		sessionManager.getSessionManagementLock().lock();
		System.out.println("         got sessionManagementLock at " + System.currentTimeMillis());		
		
		// wait until all threads have run less times than before so must have been restarted
		waitFor("less than", 5, 3000);
		System.out.println("End of SessionManagerTest\n\n");		
	}

	private void waitFor(String moreOrLess, int expectedRuns, long millis) throws InterruptedException {
		long start = System.currentTimeMillis();
		int length = sessionManager.getThreads().length;
		int nbrThreads = 0;
		while ((System.currentTimeMillis() - start < millis) && nbrThreads < length) {
			for (int i=0; i< sessionManager.getThreads().length; i++) {
				if ("more than".equals(moreOrLess)) {
					if (sessionManager.getRunnables()[i].runCount > expectedRuns) {
						nbrThreads++;
					}
				} else {
					if (sessionManager.getRunnables()[i].runCount < expectedRuns) {
						nbrThreads++;
					}
				}
			}
			if (nbrThreads < length) {
				nbrThreads = 0;
				Thread.sleep(10);
			}
		}
		checkNumberOfRuns(moreOrLess, expectedRuns);
	}
	
	
	private void checkNumberOfRuns(String moreOrLess, int expected) {
		for (int i=0; i< sessionManager.getThreads().length; i++) {
			System.out.println(String.format("Thread %s run %d times", sessionManager.getThreads()[i].getName(), sessionManager.getRunnables()[i].runCount));
		}
		for (int i=0; i< sessionManager.getThreads().length; i++) {
			assertTrue(String.format("expected %s %d runs but thread %s run %d times.", moreOrLess, expected, sessionManager.getThreads()[i].getName(), sessionManager.getRunnables()[i].runCount), 
					"more than".equals(moreOrLess) ? sessionManager.getRunnables()[i].runCount > expected : sessionManager.getRunnables()[i].runCount < expected);
		}
	}
	
}
