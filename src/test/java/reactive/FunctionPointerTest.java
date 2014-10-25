package reactive;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import reactive.exceptions.FunctionPointerException;

/**
 * {@link FunctionPointerImpl} tests
 * @author george georgovassilis
 *
 */

public class FunctionPointerTest {

	final String MSG = "Pointer was here";
	
	protected FunctionPointerImpl<String> callbackMethod(Promise<String> promise, AtomicReference<String> message){
		if (promise.isAvailable()){
			message.set(promise.get());
		}
		return new FunctionPointerImpl<String>(this, promise, message);
	}

	protected FunctionPointerImpl<String> callbackMethod_that_sets_wrong_arguments(Promise<String> promise, AtomicReference<String> message){
		if (promise.isAvailable()){
			message.set(promise.get());
		}
		return new FunctionPointerImpl<String>(this, promise, 1234);
	}
	
	protected FunctionPointer<Integer> callbackMethod_that_converts_text_to_number(Promise<String> text){
		FunctionPointerImpl<Integer> fp = new FunctionPointerImpl<Integer>(this, text);
		if (text.isAvailable()){
			fp.set(Integer.parseInt(text.get()));
		}
		return fp;
	}

	@Test
	public void test_simple_callback(){
		Promise<String> promise = new PromiseImpl<String>();
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
		Promise<String> promise = new PromiseImpl<String>();
		AtomicReference<String> ref = new AtomicReference<String>();
		
		promise.whenAvailable(callbackMethod_that_sets_wrong_arguments(promise, ref));
		
		assertFalse(promise.isAvailable());
		assertNull(ref.get());
		
		promise.set(MSG);
	}
	
	@Test
	public void test_callback_with_return_promise(){
		Promise<String> gasPriceText = new PromiseImpl<String>();
		Promise<Integer> gasPrice = (Promise<Integer>)gasPriceText.whenAvailable(callbackMethod_that_converts_text_to_number(gasPriceText));
		assertFalse(gasPrice.isAvailable());
		gasPriceText.set("100");
		assertEquals(100, gasPrice.get().intValue());
	}

}
