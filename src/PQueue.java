/**
 * TODO Priority Queue with occurrence time
 * 
 * @since jdk1.5 or upper
 * @author CCWU
 * @version 2.0
 * @date 2013.05.29
 * 
 */
public class PQueue {
	/**
	 * 
	 * @param args
	 * @throws PQueueException
	 */
	public static void main(String[] args) throws PQueueException {
		PQueue pq = new PQueue();
		for (int i = 0; i <= 10; i++) {
			Event e = new Event(i, EventType.UpdateLocation, i, i, i, i, i, i, i ,i);
			pq.enPQueue(e);
		}

		System.out.println(pq.length());

		while (!pq.isEmpty()) {
			Event e = pq.delPQueue();
			System.out.println(e.getEType() + " " + e.getTime());
		}

		System.out.println(pq.length());
	}

	private static final int MAX_EVENT_SIZE = 21474836; // the maximum length of queue
	private Event[] events; // the event array
	private int count; // the queue length

	public PQueue() {
		
		/**
		 * notice the event queue will full
		 **/
		
		events = new Event[MAX_EVENT_SIZE];
		count = 0;
	}

	/**
	 * put event into queue
	 * 
	 * @param evt
	 * @throws PQueueException
	 */
	public void enPQueue(Event evt) throws PQueueException {
		if (!isFull()) {
			events[count] = evt;
			count++;
		} else {
			throw new PQueueException("PQueue is full!");
		}
	}

	/**
	 * pop the element with oldest occurrence time
	 * First Come First Serve
	 * @return
	 * @throws PQueueException
	 */
	public Event delPQueue() throws PQueueException {
		if (!isEmpty()) {
			long min = events[0].getTime();
			int minindex = 0;

			for (int i = 1; i < count; i++) {
				if (events[i].getTime() < min) {
					min = events[i].getTime();
					minindex = i;
				}
			}

			Event et = events[minindex];
			events[minindex] = events[count - 1];
			count--;
			return et;
		} else {
			throw new PQueueException("PQueue is empty!");
		}
	}

	/**
	 * clear all events in queue
	 */
	public void makeEmpty() {
		count = 0;
	}

	/**
	 * check the quwwuw is empty or not
	 */
	public boolean isEmpty() {
		return count == 0;
	}

	/**
	 * check the queue is full or not
	 */
	public boolean isFull() {
		return count == MAX_EVENT_SIZE;
	}

	/**
	 * get the queue length
	 */
	public int length() {
		return count;
	}
}