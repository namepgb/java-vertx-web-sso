package com.example.vertx.verticle.web.auth;

import com.example.vertx.verticle.logger.Logger;
import com.example.vertx.verticle.launcher.LauncherConfig;
import com.example.vertx.verticle.web.WebServerVerticle;
import com.microsoft.aad.msal4j.*;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;

import java.net.URI;
import java.util.Collections;

public class AdminAzureADLoginCallbackHandler implements Handler<RoutingContext>
{
	private static final String AUTHORIZATION_SITE_URL_PREFIX = "https://login.microsoftonline.com/";
	private static final String RETURN_URL = "/admin/main";
	private WebServerVerticle verticle;
	
	public AdminAzureADLoginCallbackHandler(WebServerVerticle verticle)
	{
		this.verticle = verticle;
	}
	
	@Override
	public void handle(RoutingContext ctx)
	{
		try {
			final String client_key = LauncherConfig.Web.getAuzureADClientKey();
			final String client_id = LauncherConfig.Web.getAuzureADClientID();
			final String redirect_url = LauncherConfig.Web.getAuzureADLoginRedirectURL();
			final String authority = AUTHORIZATION_SITE_URL_PREFIX + LauncherConfig.Web.getAuzureADTenantID();
			final String scopes = "openid";
			final String code = ctx.queryParams().get("code");
			final IClientCredential credential = ClientCredentialFactory.createFromSecret(client_key);
			final ConfidentialClientApplication app = ConfidentialClientApplication
				.builder(client_id, credential)
				.authority(authority)
				.build();
			final AuthorizationCodeParameters auth_params = AuthorizationCodeParameters
				.builder(code, new URI(redirect_url)).scopes(Collections.singleton(scopes))
				.build();
			final IAuthenticationResult result = app.acquireToken(auth_params).get();
			final IAccount account = result.account();
			final String username = account.username();
			final String access_token = result.accessToken();
			final String id_token = result.idToken();
			
			Session session = ctx.session();
			AdminAzureADUser user = new AdminAzureADUser(username, access_token, id_token);
			ctx.setUser(user);
			if (session != null) {
				// the user has upgraded from unauthenticated to authenticated
				// session should be upgraded as recommended by owasp
				session.regenerateId();
			}
			
			ctx.request().response()
			   .putHeader("Location", RETURN_URL)
			   .setStatusCode(302)
			   .end();
		} catch (Exception e) {
			Logger.logError(this.verticle, e);
		}
	}
}
