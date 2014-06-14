/**
 * TODO Priority Queue Exception Class
 * 
 * @since jdk1.5 or upper
 * @author CCWU
 * @version 1.0
 * @date 2013.05.19
 * 
 */
public class PQueueException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PQueueException() {
		super("PQueueException");
	}

	public PQueueException(String msg) {
		super(msg);
	}
}