package reactive.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import reactive.model.User;

public class AsyncUserDao {

	interface Callback<T>{
		void success(T value);
	}
	
	private UserDao dao;
	private ExecutorService pool;
	
	public AsyncUserDao(UserDao dao, int threads){
		this.dao = dao;
		pool = Executors.newFixedThreadPool(threads);
	}
	
	public void findUserById(final int id, final Callback<User> callback){
		pool.submit(new Runnable() {
			
			@Override
			public void run() {
				User user = dao.findUserById(id);
				callback.success(user);
			}
		});
	}

	public void getStatus(final String userName, final Callback<Boolean> callback){
		pool.submit(new Runnable() {
			
			@Override
			public void run() {
				boolean status = dao.getStatusForUser(userName);
				callback.success(status);
			}
		});
	}

}
