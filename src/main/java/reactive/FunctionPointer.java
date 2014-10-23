package reactive;

import java.lang.reflect.Method;

/**
 * Keeps a reference to a method and its arguments so that it can be invoked at a later point.
 * Does so by examining the current stack trace and making a note of the calling method's name.
 * The method is a little brittle and should probably be improved.
 * @author george georgovassilis
 *
 */
public class FunctionPointer {

	private Object target;
	private Method method;
	private Object[] arguments;

	public FunctionPointer(Object target, Object...arguments) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		StackTraceElement caller = trace[2];
		String methodName = caller.getMethodName();
		Class<?> c = target.getClass();
		Method method = ReflectionUtils.findMethod(c, methodName, ReflectionUtils.toTypes(arguments));
		if (method == null)
			throw new RuntimeException("I think you are trying to get a pointer to "+methodName+", but I couldn't find that on "+c);
		method.setAccessible(true);
		this.target = target;
		this.method = method;
		this.arguments = arguments;
	}

	public Object getTarget() {
		return target;
	}

	public Method getMethod() {
		return method;
	}
	
	@Override
	public String toString() {
		return target.getClass()+"."+method;
	}
	
	public void invoke(){
		ReflectionUtils.invoke(method, target, arguments);
	}
}
