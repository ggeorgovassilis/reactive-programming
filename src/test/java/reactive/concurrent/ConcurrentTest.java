package reactive.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Test;

import reactive.CallbackAdapter;
import reactive.FunctionPointer;
import reactive.FunctionPointerImpl;
import reactive.Promise;
import reactive.PromiseImpl;
import reactive.model.User;
import static org.junit.Assert.*;

public class ConcurrentTest {

	final int THREADS = 4;
	final int DURATION_SEC = 20;
	final int TERMINATE = -1;

	AsyncUserDao userDao;
	AtomicInteger userIdCounter;
	BlockingQueue<Integer> queue;
	List<Thread> workers;
	Map<Integer, Integer> activeUsers;

	@Before
	public void setup() {
		userDao = new AsyncUserDao(new UserDao(), THREADS);
		userIdCounter = new AtomicInteger(1);
		queue = new ArrayBlockingQueue<Integer>(1000);
		workers = new ArrayList<Thread>();
		activeUsers = Collections.synchronizedMap(new HashMap<Integer, Integer>());
	}

	protected FunctionPointer<Boolean> getStatus(Promise<User> user) {
		FunctionPointer<Boolean> status = new FunctionPointerImpl<Boolean>(
				this, user);
		if (user.isAvailable()) {
			userDao.getStatus(user.get().fullName, CallbackAdapter.callback(
					AsyncUserDao.Callback.class, status));
		}
		return status;
	}

	private int getUserId() {
		return userIdCounter.incrementAndGet();
	}

	private int getNextUserIdFromQueue() {
		try {
			Integer id =  queue.take();
			if (id.intValue() == TERMINATE)
				return id;
			Integer seen = activeUsers.get(id);
			if (seen!=null)
				throw new RuntimeException("user "+id+" was seen "+seen+" times");
			activeUsers.put(id,0);
			return id;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected FunctionPointer<Void> resolveTest(Promise<User> user, Promise<Boolean> status, Integer userId){
		FunctionPointer<Void> fp = new FunctionPointerImpl<Void>(this, user, status, userId);
		if (user.isAvailable() && status.isAvailable()){
			Integer id = Integer.parseInt(user.get().customerId);
			Integer seen = activeUsers.get(id);
			if (seen.intValue()!=0)
				throw new RuntimeException(user.get().customerId+" seen "+seen+" times");
			activeUsers.put(id, seen+1);
			assertEquals("Esteemed customer " + userId,
					user.get().fullName);
			assertEquals(userId % 2 == 0, status.get()
					.booleanValue());
			fp.set(null);
		}
		return fp;
	}

	@Test
	public void concurrentTest() throws Exception{
		long start = -System.currentTimeMillis();
		final AtomicLong count = new AtomicLong();
		long timestamp = -System.currentTimeMillis();
		for (int i = 0; i < THREADS; i++) {
			Thread worker = new Thread() {
				@Override
				public void run() {
					while (true) {
						final int userId = getNextUserIdFromQueue();
						if (userId == TERMINATE)
							return;

						final Promise<User> user = new PromiseImpl<User>("worker user");
						userDao.findUserById(userId, CallbackAdapter.callback(
								AsyncUserDao.Callback.class, user));
						final Promise<Boolean> status = user
								.invokeWhenAvailable(getStatus(user));
						Promise<Void> done = status.invokeWhenAvailable(resolveTest(user, status, userId));
						done.waitForResolution();
						count.incrementAndGet();
					}
				}
			};
			worker.start();
			workers.add(worker);
		}

		while (start + System.currentTimeMillis() < DURATION_SEC * 1000){
			int userId = getUserId();
			queue.put(userId);
		}
		System.err.println("Terminating");
		for (int i=0;i<workers.size();i++)
			queue.put(TERMINATE);
		for (Thread t:workers)
			t.join();
		timestamp+=System.currentTimeMillis();
		System.out.println(count.get()/(timestamp/1000)+" runs/sec");
	}
}
