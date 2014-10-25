package reactive;

import java.util.ArrayList;
import java.util.List;

import reactive.exceptions.AlreadyResolvedException;
import reactive.exceptions.NotResolvedException;
import reactive.exceptions.PromiseException;

/**
 * Holds the future value of an invocation.
 * When someone needs a value of type T that will be available in the future, return a promise of type T.
 * The caller can register a callback via {@link #whenAvailable(Callback)} or {@link #whenAvailable(FunctionPointerImpl)}
 * which will be invoked once the value is available. We say then that the promise is resolved.
 * This class is not thread safe; concurrent access must be synchronized.
 * 
 * In order to let callers know that a value is available, use {@link #set(Object)}.
 * @author george georgovassilis
 *
 * @param <T>
 */
public class PromiseImpl<T> implements Promise<T> {

	protected T value;
	protected Exception error;
	protected boolean isSet = false;
	protected List<Callback<T>> callbacks = new ArrayList<Promise.Callback<T>>();
	protected final String name;
	
	public PromiseImpl(String name){
		this.name = name;
	}
	
	public PromiseImpl(){
		this("unnamed");
	}

	protected void invokeCallback(Callback<T> callback) {
		if (error != null)
			callback.error(error);
		else
			callback.success(value);
	}

	@Override
	public Exception getError() {
		return error;
	}

	protected void invokeCallbacks() {
		List<Callback<T>> copy = new ArrayList<>(callbacks);
		for (Callback<T> c : copy)
			invokeCallback(c);
	}

	/**
	 * Gets the value once the promise has been resolved.
	 * @return
	 */
	@Override
	public T get() throws PromiseException{
		if (!isSet)
			throw new NotResolvedException("Promise hasn't been resolved yet");
		return value;
	}

	/**
	 * Resolve the promise with value and invoke all registered callbacks
	 * @param value
	 */
	@Override
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
	@Override
	public void whenAvailable(Callback<T> callback) {
		callbacks.add(callback);
		if (isSet) 
			invokeCallback(callback);
	}

	/**
	 * Resolve the promise with a failure
	 * @param e
	 */
	@Override
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
	 * @param pointer Must be a {@link FunctionPointerImpl}
	 * @return
	 */
	@Override
	public <R> Promise<R> whenAvailable(final FunctionPointer<R> functionPointer){
		final Promise<R> returnPromise = new PromiseImpl<R>("return "+name);
		whenAvailable(new Callback<T>() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void success(T success) {
				Promise<R> result = (Promise<R>)functionPointer.invoke();
				result.whenAvailable(new Callback<R>() {

					@Override
					public void success(R success) {
						returnPromise.set(success);
					}

					@Override
					public void error(Exception e) {
						returnPromise.fail(e);
					}
				});
			}
			
			@Override
			public void error(Exception e) {
			}
		});
		return returnPromise;
	}
	
	/**
	 * Determines whether the promise has been resolved already. Only then can {@link #get()} be safely invked.
	 * @return
	 */
	@Override
	public boolean isAvailable(){
		return isSet;
	}
	
}
