package reactive;

import java.util.ArrayList;
import java.util.List;

import reactive.exceptions.AlreadyResolvedException;
import reactive.exceptions.NotResolvedException;
import reactive.exceptions.PromiseException;

/**
 * Holds the future value of an invocation.
 * When someone needs a value of type T that will be available in the future, return a promise of type T.
 * The caller can register a callback via {@link #whenAvailable(Callback)} or {@link #whenAvailable(FunctionPointer)}
 * which will be invoked once the value is available. We say then that the promise is resolved.
 * This class is not thread safe; concurrent access must be synchronized.
 * 
 * In order to let callers know that a value is available, use {@link #set(Object)}.
 * @author george georgovassilis
 *
 * @param <T>
 */
public class Promise<T> {

	public interface Callback<T> {
		void success(T success);

		void error(Exception e);
	}

	protected T value;
	protected Exception error;
	protected boolean isSet = false;
	protected List<Callback<T>> callbacks = new ArrayList<Promise.Callback<T>>();

	protected void invokeCallback(Callback<T> callback) {
		if (error != null)
			callback.error(error);
		else
			callback.success(value);
	}

	public Exception getError() {
		return error;
	}

	protected void invokeCallbacks() {
		for (Callback<T> c : callbacks)
			invokeCallback(c);
	}

	/**
	 * Gets the value once the promise has been resolved.
	 * @return
	 */
	public T get() throws PromiseException{
		if (!isSet)
			throw new NotResolvedException("Promise hasn't been resolved yet");
		return value;
	}

	/**
	 * Resolve the promise with value and invoke all registered callbacks
	 * @param value
	 */
	public void set(T value) throws PromiseException{
		if (isSet)
			throw new AlreadyResolvedException("Promise has already been resolved");
		this.value = value;
		isSet = true;
		invokeCallbacks();
	}

	/**
	 * Register a callback which will be invoked once the promise has been resolved. In case the
	 * promise has been resolved before this method is invoked, the callback will be called immediately.
	 * @param callback
	 */
	public void whenAvailable(Callback<T> callback) {
		callbacks.add(callback);
		if (isSet) 
			invokeCallbacks();
	}

	/**
	 * Resolve the promise with a failure
	 * @param e
	 */
	public void fail(Exception e) throws PromiseException{
		if (isSet)
			throw new AlreadyResolvedException("Promise has already been resolved");
		if (e == null)
			throw new PromiseException("Error can't be null");
		this.error = e;
		isSet = true;
		invokeCallbacks();
	}
	
	/**
	 * Register a function pointer as callback. Conventions of {@link #whenAvailable(Callback)} apply
	 * @param pointer
	 * @return
	 */
	public Promise<T> whenAvailable(final FunctionPointer pointer){
		whenAvailable(new Callback<T>() {
			
			@Override
			public void success(T success) {
				pointer.invoke();
			}
			
			@Override
			public void error(Exception e) {
			}
		});
		return this;
	}
	
	/**
	 * Determines whether the promise has been resolved already. Only then can {@link #get()} be safely invked.
	 * @return
	 */
	public boolean isAvailable(){
		return isSet;
	}
	
}
