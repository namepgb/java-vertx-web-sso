package com.example.vertx.verticle.web.auth;

import com.example.vertx.verticle.logger.Logger;
import com.example.vertx.verticle.launcher.LauncherConfig;
import com.example.vertx.verticle.web.WebServerVerticle;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.web.RoutingContext;

public class AdminAzureADLoginHandler implements Handler<RoutingContext>
{
	private static final String AUTHORIZATION_SITE_URL_PREFIX = "https://login.microsoftonline.com/";
	private static final String AUTHORIZATION_PATH = "/oauth2/v2.0/authorize";
	private WebServerVerticle verticle;
	
	public AdminAzureADLoginHandler(WebServerVerticle verticle)
	{
		this.verticle = verticle;
	}
	
	@Override
	public void handle(RoutingContext ctx)
	{
		try {
			final OAuth2ClientOptions options = new OAuth2ClientOptions()
				.setClientID(LauncherConfig.Web.getAuzureADClientID())
				.setClientSecret(LauncherConfig.Web.getAuzureADClientSecret())
				.setSite(AUTHORIZATION_SITE_URL_PREFIX + LauncherConfig.Web.getAuzureADTenantID())
				.setAuthorizationPath(AUTHORIZATION_PATH);
			final OAuth2Auth oauth2 = OAuth2Auth.create(verticle.getVertx(), OAuth2FlowType.AUTH_CODE, options);
			final JsonObject authorizeURL = new JsonObject()
				.put("redirect_uri", LauncherConfig.Web.getAuzureADLoginRedirectURL())
				.put("prompt", "select_account")
				.put("response_mode", "query")
				.put("scope", "openid");
			final String location = oauth2.authorizeURL(authorizeURL);
			ctx.response()
			   .putHeader("Content-Type", "text/plain")
			   .putHeader("Access-Control-Allow-Origin", "*")
			   .putHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS")
			   .putHeader("Access-Control-Max-Age", "3600")
			   .putHeader("Access-Control-Allow-Headers", "Content-Type,x-requested-with,Authorization,Axxess-Control-Allow-Origin")
			   .putHeader("Location", location)
			   .setStatusCode(302)
			   .end();
		} catch (Exception e) {
			Logger.logError(this.verticle, e);
		}
	}
}
