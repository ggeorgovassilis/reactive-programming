package reactive.legacy;

/**
 * This interface models a simple user credential verification form. It has the following fields:
 * 
 * login: the user is supposed to type his login name here. We can only read whatever the user typed here, so the interface offers only
 * a getter.
 * 
 * password: same story like login; user types password, we get read-only access to it.
 * 
 * userName: we can show the full user name once we query it from the remote service; write only.
 * 
 * userStatus: we can show the user status here once we query it from the remote service; write only.
 * 
 * customerId: just some customer id which we'll show.
 * 
 * The user is supposed to click the submit button, which we can be notified of by registering a {@link ClickListener}
 * via the {@link #setClickListenerToLoginButton(ClickListener)} method.
 * 
 * @author george georgovassilis
 *
 */
public interface LoginForm {
	
	interface ClickListener{
		void onLoginButtonClicked();
	}

	String getLogin();
	String getPassword();
	void setFullUserName(String name);
	void setUserStatus(String status);
	void setCustomerId(String id);
	void setClickListenerToLoginButton(ClickListener listener);
}
