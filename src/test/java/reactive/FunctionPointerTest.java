package reactive;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
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
		
		promise.invokeWhenAvailable(callbackMethod(promise, ref));
		
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
		
		promise.invokeWhenAvailable(callbackMethod_that_sets_wrong_arguments(promise, ref));
		
		assertFalse(promise.isAvailable());
		assertNull(ref.get());
		
		promise.set(MSG);
	}
	
	@Test
	public void test_callback_with_return_promise(){
		Promise<String> gasPriceText = new PromiseImpl<String>();
		Promise<Integer> gasPrice = (Promise<Integer>)gasPriceText.invokeWhenAvailable(callbackMethod_that_converts_text_to_number(gasPriceText));
		assertFalse(gasPrice.isAvailable());
		gasPriceText.set("100");
		assertEquals(100, gasPrice.get().intValue());
	}

	protected FunctionPointer<Integer> add(Promise<Integer> a, Promise<Integer> b, List<String> log){
		FunctionPointer<Integer> fp = new FunctionPointerImpl<Integer>(this, a, b, log);
		log.add("pass");
		if (a.isAvailable() && b.isAvailable()){
			log.add("match");
			fp.set(a.get()+b.get());
		}
		log.add("exit");
		return fp;
	}
	
	@Test
	public void test_preresolved_promises(){
		Promise<Integer> a = new PromiseImpl<Integer>();
		Promise<Integer> b = new PromiseImpl<Integer>();
		List<String> log = new ArrayList<String>();
		a.set(1);
		b.set(2);
		Promise<Integer> sum = b.invokeWhenAvailable(add(a,b, log));
		assertTrue(sum.isAvailable());
		assertEquals(3, sum.get().intValue());
		assertEquals("pass", log.get(0));
		assertEquals("match", log.get(1));
		assertEquals("exit", log.get(2));
		assertEquals(3, log.size());
	}
}
