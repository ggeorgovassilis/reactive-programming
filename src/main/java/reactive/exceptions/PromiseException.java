package reactive.exceptions;

/**
 * Base class for a wide range of more specific exceptions
 * @author george georgovassilis
 *
 */
public class PromiseException extends RuntimeException{
	
	public PromiseException(String reason){
		super(reason);
	}

}
