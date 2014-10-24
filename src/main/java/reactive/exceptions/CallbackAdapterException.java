package reactive.exceptions;

import reactive.CallbackAdapter;

/**
 * Generic exception used by the {@link CallbackAdapter}
 * @author george georgovassilis
 *
 */
public class CallbackAdapterException extends PromiseException{

	public CallbackAdapterException(String reason) {
		super(reason);
	}

}
