package monitor.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A fixed length array that wraps round and overwrites the oldest elements when capacity is exceeded.
 * When it is full the values returned by getLowestElementNumber and getHighestElementNumber increase as new elements are added.
 * An attempt to get an element before the lowest will return the lowest and a get beyond getHighestElementNumber
 * will return the highest. 
 */
public class RingBuffer<E> {
	
	static Logger logger = Logger.getLogger(RingBuffer.class.getName());
	static final boolean logFine = Configuration.getInstance().isLogFine();
	
	private int arraySize;
	private Object[] elements;
	private int writePosition = 0;	
	private int highestIndex = -1;
	private int lowestIndex = -1;

	public RingBuffer(int arraySize) {
		this.arraySize = arraySize;
		elements = new Object[arraySize];
	}
	
	public synchronized void append(E element) {
		elements[writePosition] = element;

		writePosition++;
		highestIndex++;

		if (writePosition > arraySize - 1) {
			writePosition = 0;
		}
		
		if (highestIndex > arraySize -1) {
			lowestIndex++;			
		} else {
			lowestIndex = 0;
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized E get(int elementNumber) {
		if (lowestIndex == -1) {
			return null;
		}
		int index = calculateIndex(elementNumber);
		try {
			return (E) elements[index];
		} catch (Exception e) {
			logger.log(Level.SEVERE, "wrong index calculation for elements", e);
			return null;
		}
	}

	private int calculateIndex(int elementNumber) {
		if (elementNumber > highestIndex) {
			if (logFine) logger.log(Level.INFO, String.format("requested elementNumber %d is greater than highest available: %d", elementNumber, highestIndex));
			elementNumber = highestIndex;
		}		
		if (elementNumber < lowestIndex) {
			if (logFine) logger.log(Level.INFO, String.format("requested elementNumber %d is before the oldest stored: %d", elementNumber, lowestIndex));
			elementNumber = lowestIndex;
		}		
		
		int index = elementNumber;
		
		if (highestIndex > arraySize -1) {
			if (elementNumber > highestIndex - writePosition) {
				index = writePosition - (highestIndex - elementNumber) - 1;				
			} else {
				index = writePosition + (elementNumber - lowestIndex);
			}
			
		}
		return index;
	}

	public int getHighstElementNumber() {
		return highestIndex;
	}

	public int getLowestElementNumber() {
		return lowestIndex;
	}
	
	public List<E> getAll() {
		return getElements(lowestIndex, highestIndex);
	}
	
	public List<E> getElements(int from, int to) {
		List<E> elements = new ArrayList<E>();
		if (to - from > arraySize) {
			String message = String.format("elements requested is greater than array size: to:%d - from:%d > arraySize:%d", to, from, arraySize);
			Exception e = new IndexOutOfBoundsException(message);
			e.fillInStackTrace();
			logger.log(Level.SEVERE, message, e);
			return elements;
		}
		for (int x = from; from > -1 && x <= to; x++) {
			elements.add(get(x));
			Thread.yield();
		}
		return elements;
	}
	
	/** Used to resize the array when you don't have access to the code that created it. */
	public void changeArraySize(int arraySize) {
		this.arraySize = arraySize;
		elements = new String[arraySize];
	}
	
}
