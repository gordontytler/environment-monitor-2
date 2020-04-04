package monitor.soap.client;

import java.util.logging.Logger;

public class AdaptiveSleeper {

		static Logger logger = Logger.getLogger(AdaptiveSleeper.class.getName());
	
		private int maxPoll;
		private int minPoll;
		private int step;
		private int pollDelay;		
		
		public AdaptiveSleeper(int maxPoll, int minPoll, int step) {
			super();
			this.maxPoll = maxPoll;
			this.minPoll = minPoll;
			this.step = step;
			pollDelay = minPoll;
		}
		
		public void increaseDelay() {
			if (pollDelay < maxPoll) {
				pollDelay += step;
			}
		}

		public void decreaseDelay() {
			if (pollDelay > minPoll) {
				pollDelay -= step;
			}
		}
		
		public void resetToMinimum() {
			pollDelay = minPoll;
		}
		
		public void sleep()  {
			sleep(pollDelay);
		}
		
		public void minimumSleep()  {
			sleep(minPoll);
		}

		private void sleep(int millis)  {
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				String message = "Sleep interupted. Window may be closing. Rethrowing exception to halt the calling thread.";
				logger.warning(message);
				throw new RuntimeException(message, e);
			}
		}

}
