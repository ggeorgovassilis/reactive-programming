package reactive;

import static org.junit.Assert.*;
import org.junit.Test;

public class CallbackAdapterTest {

	interface SingleValueCallback{
		
		void setValue(String string);
		
	}

	interface ValueAndErrorCallback{
		
		void success(String string);
		void fail(Exception error);
	}

	interface BaitCallback{
		
		void success(String string);
		void fakeSuccess(String string, Boolean b);
		void fail(Exception error);
	}

	@Test
	public void test_simple_callback(){
		final String value = "value";
		Promise<String> promise = new Promise<String>();
		SingleValueCallback callback = CallbackAdapter.callback(SingleValueCallback.class, promise);
		
		assertFalse(promise.isAvailable());
		callback.setValue(value);
		assertTrue(promise.isAvailable());
		assertEquals(value, promise.get());
	}

	@Test
	public void test_callback_with_value_and_error_setters(){
		final String value = "value";
		Promise<String> promise = new Promise<String>();
		ValueAndErrorCallback callback = CallbackAdapter.callback(ValueAndErrorCallback.class, promise);
		
		assertFalse(promise.isAvailable());
		callback.success(value);
		assertTrue(promise.isAvailable());
		assertEquals(value, promise.get());
	}

	@Test
	public void test_callback_with_misleading_methods(){
		final String value = "value";
		Promise<String> promise = new Promise<String>();
		BaitCallback callback = CallbackAdapter.callback(BaitCallback.class, promise);
		
		assertFalse(promise.isAvailable());
		callback.success(value);
		assertTrue(promise.isAvailable());
		assertEquals(value, promise.get());
	}
}
