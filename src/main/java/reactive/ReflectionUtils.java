package reactive;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtils {

	public static Class<?>[] toTypes(Object[] args){
		Class<?>[] types = new Class[args.length];
		for (int i=0;i<args.length;i++)
			if (args[i]!=null)
				types[i] = args[i].getClass();
		return types;
	}
	
	public static Method findMethod(Class<?> c, String methodName, Class<?>[] argumentTypes){
		if (c == null)
			return null;
		List<Method> methods = new ArrayList<Method>();
		methods.addAll(Arrays.asList(c.getMethods()));
		methods.addAll(Arrays.asList(c.getDeclaredMethods()));
		for (Method m : methods) {
			if (!m.getName().equals(methodName))
				continue;
			Class<?>[] paramTypes = m.getParameterTypes();
			if (paramTypes.length!=argumentTypes.length)
				continue;
			boolean typeMatches = true;
			for (int i=0;i<paramTypes.length;i++)
				typeMatches=typeMatches&&(argumentTypes[i]==null||paramTypes[i].isAssignableFrom(argumentTypes[i]));
			if (!typeMatches)
				continue;
			return m;
		}
		return findMethod(c.getSuperclass(), methodName, argumentTypes);
	}

	public static Object invoke(Method method, Object target, Object... arguments) throws RuntimeException{
		try {
			return method.invoke(target, arguments);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
