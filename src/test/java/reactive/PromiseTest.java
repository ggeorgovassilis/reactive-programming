package reactive;

import org.junit.Test;

import reactive.exceptions.AlreadyResolvedException;
import reactive.exceptions.NotResolvedException;
import reactive.exceptions.PromiseException;
import reactive.Promise;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Verifying {@link PromiseImpl} functionality
 * @author george georgovassilis
 *
 */

public class PromiseTest {
	
	@SuppressWarnings("unchecked")
	Callback<String> mockCallback(){
		return mock(Callback.class);
	}
	
	@Test
	public void test_set(){
		final String value = "value";
		Promise<String> p = new PromiseImpl<String>();
		assertFalse(p.isAvailable());
		p.set(value);
		assertTrue(p.isAvailable());
		assertEquals(value, p.get());
	}

	@Test(expected=NotResolvedException.class)
	public void test_get_without_set(){
		Promise<String> p = new PromiseImpl<String>();
		assertEquals("", p.get());
	}

	@Test(expected=AlreadyResolvedException.class)
	public void test_double_set(){
		final String value1 = "value1";
		final String value2 = "value2";
		Promise<String> p = new PromiseImpl<String>();
		p.set(value1);
		p.set(value2);
	}

	@Test(expected=AlreadyResolvedException.class)
	public void test_set_fail(){
		final String value = "value";
		final Exception error = new Exception();
		Promise<String> p = new PromiseImpl<String>();
		p.set(value);
		p.fail(error);
	}

	@Test(expected=AlreadyResolvedException.class)
	public void test_fail_set(){
		final String value = "value";
		final Exception error = new Exception();
		Promise<String> p = new PromiseImpl<String>();
		p.fail(error);
		p.set(value);
	}

	@Test
	public void test_errors(){
		Promise<String> p = new PromiseImpl<String>();
		final Exception error = new Exception();
		p.fail(error);
		assertTrue(p.isAvailable());
		assertEquals(error, p.getError());
	}
	
	@Test
	public void test_null_set(){
		Promise<String> p = new PromiseImpl<String>();
		p.set(null);
		assertTrue(p.isAvailable());
		assertNull(p.get());
	}

	@Test(expected=PromiseException.class)
	public void test_null_error(){
		Promise<String> p = new PromiseImpl<String>();
		p.fail(null);
	}
	
	@Test
	public void test_callback(){
		final String value = "value";
		Promise<String> p = new PromiseImpl<String>();
		Callback<String> callback = mockCallback();
		p.whenAvailable(callback);
		verify(callback, never()).set(anyString());
		p.set(value);
		verify(callback, times(1)).set(value);
	}

	@Test
	public void test_callback_error(){
		final Exception error = new Exception();
		Promise<String> p = new PromiseImpl<String>();
		Callback<String> callback = mockCallback();
		p.whenAvailable(callback);
		verify(callback, never()).fail(any(Exception.class));
		p.fail(error);
		verify(callback, times(1)).fail(error);
	}

	@Test
	public void test_multiple_callback_error(){
		final Exception error = new Exception();
		Promise<String> p = new PromiseImpl<String>();
		
		Callback<String> callback1 = mockCallback();
		Callback<String> callback2 = mockCallback();

		p.whenAvailable(callback1);
		p.whenAvailable(callback2);
		verify(callback1, never()).fail(any(Exception.class));
		verify(callback2, never()).fail(any(Exception.class));
		p.fail(error);
		verify(callback1, times(1)).fail(error);
		verify(callback2, times(1)).fail(error);
	}

	@Test
	public void test_callback_on_resolved_promise(){
		final String value = "value";
		Promise<String> p = new PromiseImpl<String>();
		Callback<String> callback = mockCallback();
		p.set(value);
		p.whenAvailable(callback);
		verify(callback, times(1)).set(value);
	}

	@Test
	public void test_callback_error_on_resolved_promise(){
		final Exception error = new Exception();
		Promise<String> p = new PromiseImpl<String>();
		Callback<String> callback = mockCallback();
		p.fail(error);
		p.whenAvailable(callback);
		verify(callback, times(1)).fail(error);
	}

}
