package reactive;

import static org.junit.Assert.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import reactive.exceptions.FunctionPointerException;

/**
 * {@link FunctionPointer} tests
 * @author george georgovassilis
 *
 */

public class FunctionPointerTest {

	final String MSG = "Pointer was here";
	
	protected FunctionPointer callbackMethod(Promise<String> promise, AtomicReference<String> message){
		if (promise.isAvailable()){
			message.set(promise.get());
		}
		return new FunctionPointer(this, promise, message);
	}

	protected FunctionPointer callbackMethod_that_sets_wrong_arguments(Promise<String> promise, AtomicReference<String> message){
		if (promise.isAvailable()){
			message.set(promise.get());
		}
		return new FunctionPointer(this, promise, 1234);
	}

	@Test
	public void test_simple_callback(){
		Promise<String> promise = new Promise<String>();
		AtomicReference<String> ref = new AtomicReference<String>();
		
		promise.whenAvailable(callbackMethod(promise, ref));
		
		assertFalse(promise.isAvailable());
		assertNull(ref.get());
		
		promise.set(MSG);
		
		assertTrue(promise.isAvailable());
		assertEquals(MSG, ref.get());
	}

	@Test(expected=FunctionPointerException.class)
	public void test_failure_with_wrong_argument_count(){
		Promise<String> promise = new Promise<String>();
		AtomicReference<String> ref = new AtomicReference<String>();
		
		promise.whenAvailable(callbackMethod_that_sets_wrong_arguments(promise, ref));
		
		assertFalse(promise.isAvailable());
		assertNull(ref.get());
		
		promise.set(MSG);
	}

}
