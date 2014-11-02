package reactive;

import reactive.exceptions.PromiseException;

/**
 * Holds the future value of an invocation.
 * When someone needs a value of type T that will be available in the future, return a promise of type T.
 * The caller can register a callback via {@link #whenAvailable(Callback)} or {@link #whenAvailable(FunctionPointer)}
 * which will be invoked once the value is available. We say then that the promise is resolved.
 * Implementations can be- but generally don't have to be thread safe; concurrent access must be synchronized.
 * 
 * In order to let callers know that a value is available, use {@link #set(Object)}.
 * @author george georgovassilis
 *
 * @param <T>
 */
public interface Promise<T> {

	/**
	 * Callback interface for {@link Promise#whenAvailable(Callback)}
	 *
	 * @param <T>
	 */
	public interface Callback<T> {
		void success(T success);

		void error(Exception e);
	}

	/**
	 * Returns the error, if any, with which this promise was resolved
	 * @return
	 */
	Exception getError();

	/**
	 * Gets the value once the promise has been resolved.
	 * @return
	 */
	T get() throws PromiseException;

	/**
	 * Resolve the promise with value and invoke all registered callbacks
	 * @param value
	 */
	void set(T value) throws PromiseException;

	/**
	 * Register a callback which will be invoked once the promise has been resolved. In case the
	 * promise has been resolved before this method is invoked, the callback will be called immediately.
	 * @param callback
	 */
	void whenAvailable(Callback<T> callback);

	/**
	 * Resolve the promise with a failure
	 * @param e
	 */
	void fail(Exception e) throws PromiseException;

	/**
	 * Register a function pointer as callback. Conventions of {@link #whenAvailable(Callback)} apply
	 * @param pointer
	 * @return
	 */
	<R> Promise<R> whenAvailable(FunctionPointer<R> pointer);

	/**
	 * Determines whether the promise has been resolved already. Only then can {@link #get()} be safely invked.
	 * @return
	 */
	boolean isAvailable();
	
	/**
	 * Blocks until either {@link #set(Object)} or {@link #fail(Exception)} is called
	 */
	void waitForResolution();
	
}