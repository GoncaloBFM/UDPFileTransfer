package t1;

/**
 * Created by gbfm on 10/12/15.
 */
public class SocketSendException extends RuntimeException{
	public SocketSendException(String s, Exception e) {
		super(s, e);
	}
}
