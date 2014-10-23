package reactive.legacy;

import reactive.model.User;

/**
 * This interface models an asynchronous service for querying user data. We must provide methods a callback which
 * the service will invoke once the requested data is available. Service method invocations don't block.
 * @author george georgovassilis
 *
 */
public interface UserService {

	interface Callback<T>{
		
		void onSuccess(T value);
		void onError(Exception e);
		
	}
	
	void getUser(String login, String password, Callback<User> callbacl);
	void isUserActive(User user, Callback<Boolean> callback);
}
