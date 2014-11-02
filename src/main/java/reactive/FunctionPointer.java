package reactive;

import java.lang.reflect.Method;

public interface FunctionPointer<T> extends Promise<T>{

	Object getTarget();

	Method getMethod();

	Object invoke();
	
}