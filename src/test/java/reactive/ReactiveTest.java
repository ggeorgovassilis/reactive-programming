package reactive;

import org.junit.Test;

import reactive.legacy.MockUserService;
import reactive.legacy.MockView;
import reactive.model.User;
import reactive.modern.ReactiveUserService;
import reactive.modern.ReactiveLoginForm;

import static org.junit.Assert.*;
public class ReactiveTest {

	@Test
	public void test(){
		MockUserService legacyUserService = new MockUserService();
		MockView legacyView = new MockView();
		
		ReactiveLoginForm view = new ReactiveLoginForm(legacyView);
		ReactiveUserService userService = new ReactiveUserService(legacyUserService);
		
		User user = new User();
		user.fullName = "test user";
		user.customerId = "12345";
		
		legacyView.setLogin("login");
		legacyView.setPassword("password");
		
		// we don't need a reference to the controller because it registers itself as a listener on the view. Thus, by
		// interacting with the view, the controller gets called.
		new UserController(userService, view);

		// this will call the UserController
		legacyView.simulateButtonClick();
		
		assertEquals(null, legacyView.getUserName());
		assertEquals(null, legacyView.getStatus());
		assertEquals(null, legacyView.getCustomerId());
		
	
		legacyUserService.resolveGetUser(user);

		assertEquals("test user", legacyView.getUserName());
		assertEquals("12345", legacyView.getCustomerId());
		assertEquals(null, legacyView.getStatus());
	
		legacyUserService.resolveIsUserActive(true);
		assertEquals("Active", legacyView.getStatus());
	}
}
