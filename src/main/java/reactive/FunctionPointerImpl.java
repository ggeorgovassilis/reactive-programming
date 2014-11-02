package reactive;

import java.lang.reflect.Method;
import java.util.Arrays;

import reactive.exceptions.FunctionPointerException;

/**
 * Keeps a reference to a method and its arguments so that it can be invoked at
 * a later point. Does so by examining the current stack trace and making a note
 * of the calling method's name. The method is a little brittle and should
 * probably be improved.
 * 
 * Instances of this class are not thread safe and concurrent access must be
 * synchronized manually.
 * 
 * @author george georgovassilis
 *
 */
public class FunctionPointerImpl<T> extends PromiseImpl<T> implements
		FunctionPointer<T> {

	private Object target;
	private Method method;
	private Object[] arguments;

	public FunctionPointerImpl(Object target, Object... arguments) {
		super("Callback on " + target);
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		Method method = null;
		Class<?> c = target.getClass();

		// walk down the stack until we find a method that a) returns a
		// FunctionPointer and b) has as parameters all the supplied argument
		// types
		Class<?>[] argumentTypes = ReflectionUtils.toTypes(arguments);
		for (int i = 0; i < trace.length && method == null; i++) {
			StackTraceElement caller = trace[i];
			String methodName = caller.getMethodName();
			Method m = ReflectionUtils.findMethod(c, methodName, Promise.class,
					argumentTypes);
			if (m != null)
				method = m;
		}
		if (method == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("Couldn't find method in trace: ");
			for (StackTraceElement e : trace)
				sb.append(e).append("\n");
			System.err.println(sb);
			throw new FunctionPointerException(
					"I couldn't find a method in the current call stack which returns a "
							+ Promise.class + " and accepts these arguments: "
							+ Arrays.toString(arguments));
		}
		method.setAccessible(true);
		this.target = target;
		this.method = method;
		this.arguments = arguments;
	}

	@Override
	public Object getTarget() {
		return target;
	}

	@Override
	public Method getMethod() {
		return method;
	}

	@Override
	public String toString() {
		return target.getClass() + "." + method;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Promise<T> invoke() {
		return (Promise<T>) ReflectionUtils.invoke(method, target, arguments);
	}

}
