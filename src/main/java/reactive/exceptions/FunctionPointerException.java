package reactive.exceptions;

import reactive.FunctionPointer;

/**
 * Generic exception used by the {@link FunctionPointer}
 * @author george georgovassilis
 *
 */

public class FunctionPointerException extends PromiseException{

	public FunctionPointerException(String reason) {
		super(reason);
	}

}
