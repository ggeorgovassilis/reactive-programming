package reactive.legacy;

import reactive.model.User;

/**
 * Mock implementation of the {@link UserService} that gives the caller access to callbacks. Unittests need
 * these callbacks to simulate delayed invocation, i.e. when data arrives later over the network.
 * @author george georgovassilis
 *
 */
public class MockUserService implements UserService{

	protected UserService.Callback<User> getUserCallback;
	protected UserService.Callback<Boolean> isUserAcCallback;
	
	/**
	 * Simulates the late arrival of User data and invokes the callback which was registered with the
	 * {@link #getUser(String, String, reactive.legacy.UserService.Callback)} call
	 * @param user
	 */
	public void resolveGetUser(User user){
		getUserCallback.onSuccess(user);
	}
	
	/**
	 * Simulates the late arrival of the user status check result and invokes the callback
	 * which was registered with the {@link #isUserActive(User, reactive.legacy.UserService.Callback)} check
	 * @param value
	 */
	public void resolveIsUserActive(Boolean value){
		isUserAcCallback.onSuccess(value);
	}
	
	@Override
	public void getUser(String login, String password, UserService.Callback<User> callback) {
		getUserCallback = callback;
	}

	@Override
	public void isUserActive(User user, UserService.Callback<Boolean> callback) {
		isUserAcCallback = callback;
	}

	
	
	
}
