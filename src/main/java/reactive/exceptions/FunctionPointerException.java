package reactive.exceptions;

import reactive.FunctionPointerImpl;

/**
 * Generic exception used by the {@link FunctionPointerImpl}
 * @author george georgovassilis
 *
 */

public class FunctionPointerException extends PromiseException{

	public FunctionPointerException(String reason) {
		super(reason);
	}

}
