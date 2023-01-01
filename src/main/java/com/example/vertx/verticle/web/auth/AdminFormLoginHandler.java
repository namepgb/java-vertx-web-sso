package com.example.vertx.verticle.web.auth;

import com.example.vertx.verticle.logger.Logger;
import com.example.vertx.verticle.web.WebServerVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.FormLoginHandler;

public class AdminFormLoginHandler implements FormLoginHandler
{
	private static final String USERNAME_PARAM = "username";
	private static final String PASSWORD_PARAM = "password";
	private static final String RETURN_URL_PARAM = "return_url";
	private static final String DIRECT_LOGGED_IN_OK_URL = "/admin/main";
	
	private WebServerVerticle verticle;
	private AuthProvider authProvider;
	private String usernameParam;
	private String passwordParam;
	private String returnURLParam;
	private String directLoggedInOKURL;
	private EventBus event_bus;
	
	public AdminFormLoginHandler(WebServerVerticle verticle, AuthProvider authProvider) {
		this.verticle = verticle;
		this.authProvider = authProvider;
		this.usernameParam = USERNAME_PARAM;
		this.passwordParam = PASSWORD_PARAM;
		this.returnURLParam = RETURN_URL_PARAM;
		this.directLoggedInOKURL = DIRECT_LOGGED_IN_OK_URL;
		this.event_bus = verticle.getVertx().eventBus();
	}
	
	@Override
	public FormLoginHandler setUsernameParam(String usernameParam) {
		this.usernameParam = usernameParam;
		return this;
	}
	
	@Override
	public FormLoginHandler setPasswordParam(String passwordParam) {
		this.passwordParam = passwordParam;
		return this;
	}
	
	@Override
	public FormLoginHandler setReturnURLParam(String returnURLParam) {
		this.returnURLParam = returnURLParam;
		return this;
	}
	
	@Override
	public FormLoginHandler setDirectLoggedInOKURL(String directLoggedInOKURL) {
		this.directLoggedInOKURL = directLoggedInOKURL;
		return this;
	}
	
	@Override
	public void handle(RoutingContext ctx) {
		HttpServerRequest req = ctx.request();
		if (req.method() != HttpMethod.POST) {
			ctx.fail(405); // Must be a POST
		} else {
			if (!req.isExpectMultipart()) {
				throw new IllegalStateException("Form body not parsed - do you forget to include a BodyHandler?");
			}
			MultiMap params = req.formAttributes();
			String username = params.get(usernameParam);
			String password = params.get(passwordParam);
			if (username == null || password == null) {
				Logger.logError(this.verticle, "No username or password provided in form - did you forget to include a BodyHandler?");
				ctx.fail(400);
			} else {
				Session session = ctx.session();
				JsonObject credentials = new JsonObject().put("username", username).put("password", password);
				this.authProvider.authenticate(credentials, res -> {
					if (res.succeeded()) {
						User user = res.result();
						ctx.setUser(user);
						if (session != null) {
							// the user has upgraded from unauthenticated to authenticated
							// session should be upgraded as recommended by owasp
							session.regenerateId();
							
							String returnURL = session.remove(this.returnURLParam);
							if (returnURL != null) {
								// Now redirect back to the original url
								doRedirect(req.response(), returnURL);
								return;
							}
						}
						// Either no session or no return url
						if (this.directLoggedInOKURL != null) {
							// Redirect to the default logged in OK page - this would occur
							// if the user logged in directly at this URL without being redirected here first from another
							// url
							doRedirect(req.response(), this.directLoggedInOKURL);
						} else {
							// Just show a basic page
							req.response().end(DEFAULT_DIRECT_LOGGED_IN_OK_PAGE);
						}
					} else {
						ctx.fail(403);
					}
				});
			}
		}
	}
	
	private void doRedirect(HttpServerResponse response, String url) {
		response.putHeader("Location", url).setStatusCode(302).end();
	}
	
	private static final String DEFAULT_DIRECT_LOGGED_IN_OK_PAGE = "" +
		"<html><body><h1>Login successful</h1></body></html>";
}
