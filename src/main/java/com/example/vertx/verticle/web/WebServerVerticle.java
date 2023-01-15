package com.example.vertx.verticle.web;

import com.example.vertx.verticle.launcher.LauncherConfig;
import com.example.vertx.verticle.logger.Logger;
import com.example.vertx.verticle.WrappedAbstractVerticle;
import com.example.vertx.verticle.web.auth.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.WebEnvironment;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.templ.jade.JadeTemplateEngine;

public class WebServerVerticle extends WrappedAbstractVerticle
{
	protected Router router;
	protected JadeTemplateEngine jade_template_engine;
	protected StaticHandler static_handler;
	protected AuthProvider form_auth_provider;
	protected AuthProvider totp_auth_provider;
	protected SessionHandler session_handler;
	
	@Override
	protected void onStarted(Handler<AsyncResult<Void>> handler)
	{
		Future.succeededFuture()
			  .compose(comp -> Future.future(this::initJadeTemplateEngine))
			  .compose(comp -> Future.future(this::initStaticandler))
			  .compose(comp -> Future.future(this::initAuthProvider))
			  .compose(comp -> Future.future(this::initSessionHandler))
			  .compose(comp -> Future.future(this::initHTTPWebServer))
			  .setHandler(handler);
	}
	
	protected void initJadeTemplateEngine(Handler<AsyncResult<Void>> handler)
	{
		System.setProperty(WebEnvironment.SYSTEM_PROPERTY_NAME, "Development");
		this.jade_template_engine = JadeTemplateEngine.create(this.vertx);
		this.jade_template_engine.getJadeConfiguration().setCaching(false);
		handler.handle(Future.succeededFuture());
	}
	
	protected void initStaticandler(Handler<AsyncResult<Void>> handler)
	{
		this.static_handler = StaticHandler.create();
		this.static_handler.setCachingEnabled(false);
		this.static_handler.setMaxAgeSeconds(1L);
		this.static_handler.setCacheEntryTimeout(1L);
		this.static_handler.setMaxCacheSize(1);
		handler.handle(Future.succeededFuture());
	}
	
	protected void initAuthProvider(Handler<AsyncResult<Void>> handler)
	{
		this.form_auth_provider = new AdminFormLoginAuthProvider(WebServerVerticle.this);
		this.totp_auth_provider = new AdminGoogleTOTPAuthProvider(WebServerVerticle.this);
		handler.handle(Future.succeededFuture());
	}
	
	protected void initSessionHandler(Handler<AsyncResult<Void>> handler)
	{
		this.session_handler = SessionHandler.create(LocalSessionStore.create(this.vertx))
											 .setAuthProvider(this.form_auth_provider)
											 .setSessionTimeout(LauncherConfig.Web.getSessionTimeout());
		handler.handle(Future.succeededFuture());
	}
	
	protected void initHTTPWebServer(Handler<AsyncResult<Void>> handler)
	{
		try {
			this.router = Router.router(this.vertx);
			this.router.route().handler(CookieHandler.create());
			this.router.route().handler(BodyHandler.create());
			this.router.route().handler(this.session_handler);
			this.router.route("/api/login/idpw").handler(new AdminFormLoginHandler(WebServerVerticle.this, this.form_auth_provider));
			this.router.route("/api/login/idpw/totp").handler(new AdminGoogleTOTPLoginHandler(WebServerVerticle.this, this.totp_auth_provider));
			this.router.route("/api/login/idpw/totp/regist").handler(new AdminGoogleTOTPAuthenticator.HandleRegist());
			this.router.route("/api/login/azuread").handler(new AdminAzureADLoginHandler(WebServerVerticle.this));
			this.router.route("/api/login/azuread/callback").handler(new AdminAzureADLoginCallbackHandler(WebServerVerticle.this));
			this.router.route("/api/logout").handler(new HandleLogout());
			this.router.route("/admin/login").handler(new HandleJadePage());
			this.router.route("/admin/*").handler(RedirectAuthHandler.create(this.form_auth_provider, "/admin/login", "return_url"));
			this.router.route("/admin/*").handler(new HandleJadePage());
			this.router.route().handler(this.static_handler);

			HttpServerOptions options = new HttpServerOptions()
				.setPort(LauncherConfig.Web.getWebPort())
				.setLogActivity(LauncherConfig.Web.getWebLogActivity());
			getVertx().createHttpServer(options)
					  .requestHandler(this.router)
					  .listen(ar -> {
						  if (ar.failed()) {
							  handler.handle(Future.failedFuture(ar.cause()));
						  } else {
							  handler.handle(Future.succeededFuture());
						  }
					  });
		} catch (Exception e) {
			handler.handle(Future.failedFuture(e));
		}
	}
	
	protected class HandleLogout implements Handler<RoutingContext>
	{
		@Override
		public void handle(RoutingContext ctx)
		{
			User user = ctx.user();
			if (null == user) {
				ctx.fail(403);
				return;
			}
			
			JsonObject principal = user.principal();
			Logger.logInfo(WebServerVerticle.this, String.format("logout user principal(%s)", principal.encodePrettily()));
			
			ctx.clearUser();
			ctx.response().putHeader("location", "/admin/login").setStatusCode(302).end();
		}
	}
	
	protected class HandleJadePage implements Handler<RoutingContext>
	{
		@Override
		public void handle(RoutingContext ctx)
		{
			String request_path = ctx.request().path();
			String full_path = String.format("webroot/jade/template%s", request_path);
			WebServerVerticle.this.jade_template_engine.render(ctx.data(), full_path, ar -> {
				if (ar.failed()) {
					Logger.logError(WebServerVerticle.this, "jade template engine render failed", ar.cause());
					ctx.fail(ar.cause());
				} else {
					User user = ctx.user();
					ctx.put("test", "test");
					if (null != user && null != user.principal())
						ctx.put("principal", user.principal());
					ctx.response().putHeader("content-type", "text/html");
					ctx.response().putHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
					ctx.response().putHeader("Pragma", "no-cache");
					ctx.response().putHeader("Expires", "0");
					ctx.response().end(ar.result());
				}
			});
		}
	}
}