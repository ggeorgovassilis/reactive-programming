package reactive;

import org.junit.Test;

import reactive.legacy.MockUserService;
import reactive.legacy.MockView;
import reactive.model.User;
import reactive.modern.ReactiveUserService;
import reactive.modern.ReactiveLoginForm;
import static org.junit.Assert.*;

/**
 * Integration test that verifies the entire package: {@link PromiseImpl}, {@link FunctionPointerImpl}, {@link CallbackAdapter}
 * @author george georgovassilis
 *
 */

public class UserControllerTest {

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
	
	@Test
	public void testDifferentImplementation(){
		MockUserService legacyUserService = new MockUserService();
		MockView legacyView = new MockView();
		
		User user = new User();
		user.fullName = "test user";
		user.customerId = "12345";

		ADifferentUserController controller = new ADifferentUserController(legacyUserService, legacyView);
		
		controller.doLogin("login", "password");

		assertEquals(null, legacyView.getUserName());
		assertEquals(null, legacyView.getStatus());
		assertEquals(null, legacyView.getCustomerId());
	
		legacyUserService.resolveGetUser(user);

		assertEquals(null, legacyView.getUserName());
		assertEquals(null, legacyView.getStatus());
		assertEquals(null, legacyView.getCustomerId());

		legacyUserService.resolveIsUserActive(true);


		assertEquals("test user", legacyView.getUserName());
		assertEquals("Active", legacyView.getStatus());
		assertEquals("12345", legacyView.getCustomerId());
	}
	
}
