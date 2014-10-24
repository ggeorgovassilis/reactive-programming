reactive-programming
====================

## What's wrong with current implementations of reactive programming ?

The fuss about reactive programming, futures and promises has been bugging me for a long time. Countless articles and blog posts
evangelize the use of promises as the cure for the asynchronous callback hell, but the code such frameworks produce doesn't look
at all clean or comprehensible to me: _it still has callbacks, and a lot of them_.

Reactive programming allows handling asynchronous program execution in a sequential programming style by using placeholders (promises)
for future values. If every involved API supported promises, then every reactive programme would be a sequential declaration of steps.

Let's say you get a login and password from a form, retrieve user information from a remote service and show that information to the user.
A programme with an asynchronous API would look like this:

```java

public void logUserIn(){
	String login = form.getLogin();
	String password = form.getPassword();

	userService.checkUser(login, password, new Callback{

	void onResultAvailable(User user){
		form.showUserName(user.getFullName());
		}
	});
}

```

Now, assuming that the user service could return a promise which would resolve some time in the future, we could
rewrite the programme like so:

```java

public void logUserIn(){
	String login = form.getLogin();
	String password = form.getPassword();

	Promise<User> promise = userService.checkUser(login, password);
	
	promise.then(new Callback{

	void onResultAvailable(User user){
		form.showUserName(user.getFullName());
		}
	});
}

```

That's not at all better than the first, callback-ed version. Everything is fine in reactive-world, but the execution transition from
the reactive code to the old, traditional code must happen via callbacks. If however the login form was also able to
process futures, then we could reformulate:

```java

public void logUserIn(){
	String login = form.getLogin();
	String password = form.getPassword();

	Promise<User> promise = userService.checkUser(login, password);
	form.showUserName(promise);
}

```

Now _that's_ elegant. Of course, someone has somewhere to pay the bill, and that probably is the concrete implementation of the login form which has to deal
with callbacks again...

## Can we do better?

In this repository I'm experimenting with a few different approaches to eliminating callbacks. From a technical point of view, callbacks are absolutely necessary in
order to transfer control from a decoupled, asynchronous event to "our" part of the code. But callbacks are boilerplate code, there is not much intelligence in them,
so it must be possible to eliminate some of the repetition.

## Example: UI event listeners

A prime example of callbacks are UI event listeners. I.e.:

```java
submitButton.addClickListener(new ClickListener(){

void onClick(ClickEvent event){
...
}
}
```

It would come quite handy if we could specify a method instead of the complicated callback, i.e:


```java

void onButtonClicked(ClickEvent event){
...
}

submitButton.addClickListener(onButtonClicked);

}
```

But we know that's impossible in Java since there are no function pointers. Java 8 would probably allow a closure:

```java

void onButtonClicked(ClickEvent event){
...
}

submitButton.addClickListener(event->onButtonClicked(event));

}
```


The code in this repository implements a kind of function pointers for Java without closures. In the end, you can write something similar to this:

```java

FunctionPointer onSubmitButtonClicked(Promise<ClickEvent> buttonClicked) {
	if (buttonClicked.isAvailable()) {
		String login = view.getLogin();
		String password = view.getPassword();
		Promise<User> user = service.getUser(login, password);
		view.showUser(user);

		Promise<Boolean> status = service.getStatus(user);
		view.showUserStatus(status);
	}
	return new FunctionPointer(this, buttonClicked);

}

void setup(){
	Promise<ClickEvent> buttonClickedAction = view.getLoginButtonAction();
	buttonClickedAction.whenAvailable(onSubmitButtonClicked(buttonClickedAction));
}
```

The section ```new FunctionPointer(this, arguments)``` creates a pointer to whatever method we're currently in which belongs to ```this```.
Unsurprisingly, under the hood it works with reflection and by inspecting the current stack so the approach depends on a few good-will preconditions
like the JVM being able to assemble a stracktrace. Currently the implementation is super-brittle: you _really_ must construct and return the function
pointer as given in the example, no nesting or inner classes, otherwise the implementation will pick the wrong method name from the stack because it
assumes that the method name is the last-to-last element in the stack trace. Also, the method must be a method on the calling class, so no inner/anonymous classes.

The second convention requires that the callback method _always_ returns that function pointer.

The third convention requires that the method checks whether the passed in promise has been resolved already. The obvious question is, can't the library
do that check for us? It could, but we need to go into the method at least once in order to construct the function pointer, and that has to happen before the
promise has been resolved.

## Example: Service callbacks

Another example from the unit tests is a fictional ```UserService``` which returns, in an asynchronous manner, a user when given login and password as so:

```java
userService.getUser(login, password, new Callback(){
	void available(User user){
		...
	}
};
```

The library provides a convenience utility ```CallbackToPromiseAdapter.callback(Class, Promise)``` which, again via proxies and reflection, auto-generates
a callback of type ```Class``` which will resolve ```Promise``` when the callback is called. All it does is proxy the ```Class``` interface and
look for single-argument methods which can resolve ```Promise```.

So the example becomes:

```java
Promise<User> promise = new Promise<>();

userService.getUser(login, password, CallbackAdapter.callback(Callback.class, promise);
```

## Combining both examples: Get login and password from a form, check the server, show results

```java
import static CallbackToPromiseAdapter.callback;

...

FunctionPointer onUserAvailable(Promise<User> user){
	if (user.isAvailable()){
		form.showFullUserName(user.getValue().getFullUserName());
	}
	return new FunctionPointer(this, user);
}

void doLogin(){
	String login = form.getLogin();
	String password = form.getPassword();

	Promise<User> userPromise = new Promise<User>();
	userService.getUser(login, password, callback(Callback.class, userPromise);

	userPromise.whenAvailable(onUserAvailable(userPromise));
}
```