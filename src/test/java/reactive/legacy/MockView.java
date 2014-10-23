package reactive.legacy;


/**
 * Simple in-memory implementation of the view. It just stores everything we write in variables which can
 * be read out later, which is nice for testing.
 * @author george georgovassilis
 *
 */

public class MockView implements LoginForm{

	private String login;
	private String password;
	private String userName;
	private String status;
	private String customerId;
	private ClickListener clickListener;
	
	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUserName() {
		return userName;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String getLogin() {
		return login;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public void setFullUserName(String name) {
		this.userName = name;
	}

	@Override
	public void setUserStatus(String status) {
		this.status = status;
	}

	@Override
	public void setClickListenerToLoginButton(ClickListener listener) {
		this.clickListener = listener;
	}
	
	/**
	 * Simulate the user clicking on the submit button by calling the registered click listener
	 */
	public void simulateButtonClick(){
		clickListener.onLoginButtonClicked();
	}

}
