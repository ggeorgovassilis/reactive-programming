package reactive.exceptions;

/**
 * Thrown when attempting to resolve a promise a second time
 * @author george georgovassilis
 *
 */
public class AlreadyResolvedException extends PromiseException{

	public AlreadyResolvedException(String reason) {
		super(reason);
	}

}
