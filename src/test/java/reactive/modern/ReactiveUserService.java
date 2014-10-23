package reactive.modern;

import reactive.FunctionPointer;
import reactive.Promise;
import reactive.legacy.UserService;
import reactive.model.User;
import static reactive.CallbackToPromiseAdapter.callback;

/**
 * Reactive wrapper around the legacy, asynchronous {@link UserService}
 * @author george georgovassilis
 *
 */
public class ReactiveUserService {

	//reference to the legacy user service
	private UserService service;
	
	public ReactiveUserService(UserService service){
		this.service = service;
	}
	
	/**
	 * Calls {@link UserService#getUser(String, String, reactive.legacy.UserService.Callback)}
	 * and returns a promise which will resolve once getUser returns.
	 * @param login
	 * @param password
	 * @return
	 */
	public Promise<User> getUser(String login, String password){
		Promise<User> promise = new Promise<User>();
		service.getUser(login, password, callback(UserService.Callback.class, promise));
		return promise;
	}
	
	/**
	 * Our callback method which will be called once userPromise resolves.
	 * As per convention, the method has to check whether userPromise is available because
	 * it's quite likely that the method will be called before userPromise resolves.
	 * In any case, the method must always return a {@link FunctionPointer} to itself with the
	 * current arguments.
	 * @param userPromise
	 * @param statusPromise
	 * @return
	 */
	protected FunctionPointer userAvailable(Promise<User> userPromise, Promise<Boolean> statusPromise){
		if (userPromise.isAvailable()){
			User user = userPromise.get();
			service.isUserActive(user, callback(UserService.Callback.class, statusPromise));
		}
		return new FunctionPointer(this, userPromise, statusPromise);
	}
	
	/**
	 * Calls the legacy {@link UserService#isUserActive(User, reactive.legacy.UserService.Callback)}
	 * and returns a promise which will resolve once the status check finishes.
	 * @param user
	 * @return
	 */
	public Promise<Boolean> getStatus(Promise<User> user){
		Promise<Boolean> status = new Promise<Boolean>();
		user.whenAvailable(userAvailable(user, status));
		return status;
	}
}
