package monitor.implementation.environment;

import java.util.logging.Logger;

import monitor.model.EnvironmentView;

/**
 * Simulate the load of a busy client but bypass the SOAP layer.
 * If CPU % is drastically decreased this means an alternative to 
 * javax.jws.WebService is needed.
 */

public class LoadTestThread implements Runnable {

	static Logger logger = Logger.getLogger(LoadTestThread.class.getName());
	
	private EnvironmentViewBuilder environmentViewBuilder = EnvironmentViewBuilder.getInstance("colour bar test");	
	
	public void run() {
		logger.info("Running environmentViewBuilder load simulator. Uncomment in Main.java if you don't want this.");
		// @see Env  AdaptiveSleeper sleeper = new AdaptiveSleeper(10000, 500, 200);
		// AdaptiveSleeper sleeper = new AdaptiveSleeper(10000, 500, 200);*/
		long outputHistoryTimeStamp = 0l;
		int loopCount = 0;
		try {
			while (true) {
				// In EnvironmentViewScrollPane the fastest refresh is 500ms
				// 		AdaptiveSleeper sleeper = new AdaptiveSleeper(10000, 500, 200);
				Thread.sleep(500);
				EnvironmentView view = environmentViewBuilder.getEnvironmentView(outputHistoryTimeStamp);
				outputHistoryTimeStamp = view.getOutputHistoryTimeStamp();
				
				if (loopCount++ % 20 == 0) {
					new ThreadLister().listAllThreads();	
				}
				
			}
		} catch (InterruptedException e) {
		}   
	}

}
