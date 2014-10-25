package reactive;

import reactive.model.User;
import reactive.modern.ReactiveUserService;
import reactive.modern.ReactiveLoginForm;

/**
 * Our neat, 100% reactive controller. All legacy components are wrapped in
 * their respective reactive counterparts, so the controller enjoys the beauty
 * of working with linear code on a pure reactive API.
 * 
 * @author george georgovassilis
 *
 */
public class UserController {

	protected ReactiveUserService service;
	protected ReactiveLoginForm view;

	/**
	 * Constructs a controller
	 * 
	 * @param service
	 * @param view
	 */
	public UserController(ReactiveUserService service, ReactiveLoginForm view) {
		this.service = service;
		this.view = view;
		Promise<Void> buttonClickedAction = view.getLoginButtonAction();
		buttonClickedAction
				.whenAvailable(onSubmitButtonClicked(buttonClickedAction));
	}

	protected FunctionPointerImpl<User> onSubmitButtonClicked(Promise<Void> buttonClicked) {
		if (buttonClicked.isAvailable()) {
			String login = view.getLogin();
			String password = view.getPassword();
			Promise<User> user = service.getUser(login, password);
			view.showUser(user);

			Promise<Boolean> status = service.getStatus(user);
			view.showUserStatus(status);
		}
		return new FunctionPointerImpl<User>(this, buttonClicked);

	}
}
