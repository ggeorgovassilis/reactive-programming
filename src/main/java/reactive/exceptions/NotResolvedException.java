package reactive.exceptions;

/**
 * Thrown when a promise is probed for a value before it has been resolved
 * @author george georgovassilis
 *
 */

public class NotResolvedException extends PromiseException{

	public NotResolvedException(String reason) {
		super(reason);
	}

}
