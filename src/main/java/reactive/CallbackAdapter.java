package reactive;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import reactive.exceptions.CallbackAdapterException;

/**
 * Utility to make it easier dealing with legacy asynchronous callbacks. It will
 * construct dynamic proxies which implement callback interfaces. When methods
 * on those callback interfaces are called, it will resolve the promises with
 * those methods' arguments.
 * 
 * @author george georgovassilis
 *
 */
public class CallbackAdapter {

	protected static void invokeSetter(Promise<?> promise, Object value) {
		Method[] methods = promise.getClass().getMethods();
		for (Method m : methods) {
			Class<?>[] argTypes = m.getParameterTypes();
			if (argTypes.length == 1) {
				if (value == null
						|| argTypes[0].isAssignableFrom(value.getClass())) {
					ReflectionUtils.invoke(m, promise, value);
					return;
				}
			}
		}
		throw new CallbackAdapterException("Didn't find setter on promise");
	}

	protected static Object handleBaseMethods(Method m, Object[] args,
			Object proxy) {
		if ("equals".equals(m.getName()) && (args != null && args.length == 1)) {
			return proxy == args[0];
		}
		if ("hashcode".equals(m.getName())
				&& (args == null || args.length == 0)) {
			return System.identityHashCode(proxy);
		}
		throw new CallbackAdapterException("Don't know what to do with method "
				+ m);
	}

	/**
	 * Constructs a dynamic proxy which implements callbackClass. The proxy will
	 * look for a method on the callback class which gets a single argument and
	 * will intercept that method When the method is called, the proxy will
	 * resolve the provided promise with that value.
	 * 
	 * @param callbackClass
	 * @param promise
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T callback(Class<T> callbackClass,
			final Promise<?> promise) {
		return (T) Proxy.newProxyInstance(
				CallbackAdapter.class.getClassLoader(),
				new Class[] { callbackClass }, new InvocationHandler() {

					@Override
					public Object invoke(Object proxy, Method method,
							Object[] args) throws Throwable {
						Class<?>[] arguments = method.getParameterTypes();
						if (arguments.length == 1 && args.length == 1) {
							invokeSetter(promise, args[0]);
							return null;
						}
						handleBaseMethods(method, args, proxy);
						throw new CallbackAdapterException(
								"Don't know what to do with " + method);
					}
				});
	}
}
