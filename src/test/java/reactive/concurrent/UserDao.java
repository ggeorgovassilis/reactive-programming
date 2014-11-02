package reactive.concurrent;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import reactive.model.User;

public class UserDao {

	AtomicInteger pause = new AtomicInteger(1);
	
	void sleep() {
//		int p = pause.incrementAndGet();
//		if (p>10)
//			pause.set(1);
//		try {
//			Thread.sleep(p);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		Thread.yield();
	}

	public User findUserById(int id) {
		sleep();
		User user = new User();
		user.customerId = "" + id;
		user.fullName = "Esteemed customer " + id;
		return user;
	}

	public boolean getStatusForUser(String userName) {
		sleep();
		String[] parts = userName.split(" ");
		int id = Integer.parseInt(parts[2]);
		return id % 2 == 0;
	}
}
