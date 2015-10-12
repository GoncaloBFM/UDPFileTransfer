package t1;

import java.net.SocketException;

/**
 * Created by gbfm on 10/12/15.
 */
public class SocketCreateException extends RuntimeException {

	public SocketCreateException(String s, SocketException e) {
		super(s, e);
	}
}
