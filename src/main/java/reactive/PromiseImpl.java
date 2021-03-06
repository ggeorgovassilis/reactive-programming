package reactive;

import java.util.ArrayList;
import java.util.List;

import reactive.exceptions.AlreadyResolvedException;
import reactive.exceptions.NotResolvedException;
import reactive.exceptions.PromiseException;

/**
 * Holds the future value of an invocation. When someone needs a value of type T
 * that will be available in the future, return a promise of type T. The caller
 * can register a callback via {@link #whenAvailable(Callback)} or
 * {@link #invokeWhenAvailable(FunctionPointerImpl)} which will be invoked once the
 * value is available. We say then that the promise is resolved. This class is
 * not thread safe; concurrent access must be synchronized.
 * 
 * In order to let callers know that a value is available, use
 * {@link #set(Object)}.
 * 
 * @author george georgovassilis
 *
 * @param <T>
 */
public class PromiseImpl<T> implements Promise<T> {

	protected T value;
	protected Exception error;
	protected boolean isSet = false;
	protected List<Callback<T>> callbacks = new ArrayList<Callback<T>>();
	protected final String name;

	public PromiseImpl(String name) {
		this.name = name;
	}

	public PromiseImpl() {
		this("unnamed");
	}

	protected void invokeCallback(Callback<T> callback) {
		if (error != null)
			callback.fail(error);
		else
			callback.set(value);
	}

	@Override
	public Exception getError() {
		return error;
	}

	protected void invokeCallbacks() {
		synchronized (callbacks) {
			for (Callback<T> c : callbacks)
				invokeCallback(c);
		}
	}

	@Override
	public T get() throws PromiseException {
		synchronized (callbacks) {
			if (!isSet)
				throw new NotResolvedException(
						"Promise hasn't been resolved yet");
			return value;
		}
	}

	protected void resolve() {
		synchronized (callbacks) {
			isSet = true;
			invokeCallbacks();
			callbacks.notifyAll();
		}
	}

	@Override
	public void set(T value) throws PromiseException {
		synchronized (callbacks) {
			if (isSet)
				throw new AlreadyResolvedException(
						"Promise has already been resolved: " + toString());
			this.value = value;
			resolve();
		}
	}

	@Override
	public void whenAvailable(Callback<T> callback) {
		synchronized (callbacks) {
			if (callbacks.contains(callback))
				throw new PromiseException("Don't register callbacks twice");
			if (isSet)
				invokeCallback(callback);
			else
				callbacks.add(callback);
		}
	}

	@Override
	public void fail(Exception e) throws PromiseException {
		synchronized (callbacks) {

			if (isSet)
				throw new AlreadyResolvedException(
						"Promise has already been resolved");
			if (e == null)
				throw new PromiseException("Error can't be null");
			this.error = e;
			resolve();
		}
	}

	@Override
	public <R> Promise<R> invokeWhenAvailable(final FunctionPointer<R> functionPointer) {
		synchronized (callbacks) {
			/**
			 * A rundown of this construct: When this promise resolves, it will
			 * call the function pointer. The function pointer may return as the
			 * result of its invocation another promise. That promise isn't
			 * necessarily immediately resolved. Since every invocation of
			 * functionPointer will return a different instance of a promise, we
			 * can't just "final promise = ..." and remember the first promise
			 * ever returned.
			 * 
			 * Thus, once "this" promise resolves, it will call the function
			 * pointer. The function pointer returns a promise which may, or may
			 * not have been resolved. In any case, we listen to that result
			 * promise and once it resolves we resolve correspondingly the
			 * returnPromise
			 */

			final Promise<R> returnPromise = new PromiseImpl<R>(
					"returnPromise from PromiseImpl.whenAvailable(FP) " + name);
			whenAvailable(new Callback<T>() {

				@SuppressWarnings("unchecked")
				@Override
				public void set(T success) {
					synchronized (callbacks) {
						Promise<R> result = null;
						if (functionPointer.isAvailable())
							result = (Promise<R>) functionPointer;
						else
							result = (Promise<R>) functionPointer.invoke();
						result.whenAvailable(returnPromise);
					}
				}

				@Override
				public void fail(Exception e) {
				}
			});
			return returnPromise;
		}
	}

	@Override
	public boolean isAvailable() {
		synchronized (callbacks) {
			return isSet;
		}
	}

	@Override
	public void waitForResolution() {
		synchronized (callbacks) {
			if (isAvailable())
				return;
			try {
				callbacks.wait();
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public String toString() {
		return "PromiseImpl " + name + ": "
				+ (isSet ? "resolved" : "unresolved");
	}

}
