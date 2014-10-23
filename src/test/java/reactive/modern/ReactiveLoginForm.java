package reactive.modern;

import reactive.FunctionPointer;
import reactive.Promise;
import reactive.legacy.LoginForm;
import reactive.model.User;

/**
 * A wrapper around the legacy {@link LoginForm} which knows how to deal with promises. 
 * @author george georgovassilis
 *
 */
public class ReactiveLoginForm {

	// reference to the real (legacy) login form
	protected LoginForm loginForm;
	
	public String getLogin(){
		return loginForm.getLogin();
	}
	
	public String getPassword(){
		return loginForm.getPassword();
	}
	
	public ReactiveLoginForm(LoginForm loginForm){
		this.loginForm = loginForm;
	}
	
	/**
	 * This method will be called when the user promise resolves. By necessary convention
	 * the method _has_ to check whether the promise is available since it is almost certain
	 * that the method will be called at least once with a not-yet-resolved promise.
	 * 
	 * @param promise
	 * @return The method must return a function pointer to itself with the current arguments.
	 */
	protected FunctionPointer onUserAvailable(Promise<User> promise){
		if (promise.isAvailable()){
			User user = promise.get();
			loginForm.setFullUserName(user.fullName);
			loginForm.setCustomerId(user.customerId);
		}
		return new FunctionPointer(this, promise);
	}
	
	/**
	 * Will show user once available
	 * @param user
	 */
	public void showUser(Promise<User> user){
		user.whenAvailable(onUserAvailable(user));
	}
	
	protected FunctionPointer onUserStatusAvailable(Promise<Boolean> promise){
		if (promise.isAvailable()){
			boolean status = promise.get();
			loginForm.setUserStatus(status?"Active":"Inactive");
		}
		return new FunctionPointer(this, promise);
	}

	/**
	 * Will show user status once available
	 * @param status
	 */
	public void showUserStatus(Promise<Boolean> status){
		status.whenAvailable(onUserStatusAvailable(status));
	}
	
	/**
	 * Register a click listener with the legacy form's submit button and return a promise
	 * which will resolve when the button is clicked.
	 * @return
	 */
	public Promise<Void> getLoginButtonAction(){
		final Promise<Void> promise = new Promise<Void>();
		loginForm.setClickListenerToLoginButton(new LoginForm.ClickListener() {
			@Override
			public void onLoginButtonClicked() {
				promise.set(null);
			}
		});
		return promise;
	}

}
