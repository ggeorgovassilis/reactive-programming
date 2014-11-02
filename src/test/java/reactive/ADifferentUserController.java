package reactive;

import reactive.legacy.MockView;
import reactive.legacy.UserService;
import reactive.model.User;

/**
 * Shows a different way of working with legacy (asynchronous) resources by using function pointers.
 * @author george georgovassilis
 *
 */
public class ADifferentUserController {

	protected UserService userService;
	protected MockView view;
	
	public ADifferentUserController(UserService userService, MockView view){
		this.userService = userService;
		this.view = view;
	}
	
	@SuppressWarnings("unchecked")
	FunctionPointer<Boolean> getUserStatus(Promise<User> user){
		FunctionPointer<Boolean> status = new FunctionPointerImpl<Boolean>(this, user);
		if (user.isAvailable()){
			userService.isUserActive(user.get(), CallbackAdapter.callback(UserService.Callback.class, status));
		}
		return status;
	}
	
	FunctionPointer<Void> showUserDetails(Promise<User> user, Promise<Boolean> status){
		if (user.isAvailable() && status.isAvailable()){
			view.setFullUserName(user.get().fullName);
			view.setCustomerId(user.get().customerId);
			view.setUserStatus(status.get()?"Active":"Inactive");
		}
		return new FunctionPointerImpl<Void>(this, user, status);
	}
	
	@SuppressWarnings("unchecked")
	public void doLogin(String login, String password){
		Promise<User> user = new PromiseImpl<>();
		userService.getUser(login, password, CallbackAdapter.callback(UserService.Callback.class, user));
		Promise<Boolean> status = user.invokeWhenAvailable(getUserStatus(user));
		status.invokeWhenAvailable(showUserDetails(user, status));
	}
}
