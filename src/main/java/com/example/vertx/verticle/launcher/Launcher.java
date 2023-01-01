package com.example.vertx.verticle.launcher;

import com.example.vertx.verticle.WrappedAbstractVerticle;
import com.example.vertx.verticle.logger.Logger;
import com.example.vertx.verticle.web.WebServerVerticle;
import io.vertx.core.*;

@SuppressWarnings({"unused"})
public class Launcher extends WrappedAbstractVerticle
{
	@Override
	protected void onStarted(Handler<AsyncResult<Void>> handler)
	{
		Future.succeededFuture()
			  .compose(comp -> Future.future(this::printConfig))
			  .compose(comp -> Future.future(this::deployVerticles))
			  .setHandler(ar -> {
			  	if (ar.failed()) {
					Logger.logError(this, ar.cause());
			  		handler.handle(Future.failedFuture(ar.cause()));
				} else {
			  		handler.handle(Future.succeededFuture());
				}
			  });
	}
	
	protected void printConfig(Handler<AsyncResult<Void>> handler)
	{
		try {
			Logger.logInfo(Launcher.this, String.format("config: web port(%d)", LauncherConfig.Web.getWebPort()));
			Logger.logInfo(Launcher.this, String.format("config: web log activity(%b)", LauncherConfig.Web.getWebLogActivity()));
			Logger.logInfo(Launcher.this, String.format("config: web admin username(%s)", LauncherConfig.Web.getAdminUserName()));
			Logger.logInfo(Launcher.this, String.format("config: web admin password(%s)", LauncherConfig.Web.getAdminPassword()));
			Logger.logInfo(Launcher.this, String.format("config: web azure ad client id(%s)", LauncherConfig.Web.getAuzureADClientID()));
			Logger.logInfo(Launcher.this, String.format("config: web azure ad tenant id(%s)", LauncherConfig.Web.getAuzureADTenantID()));
			Logger.logInfo(Launcher.this, String.format("config: web azure ad client key(%s)", LauncherConfig.Web.getAuzureADClientKey()));
			Logger.logInfo(Launcher.this, String.format("config: web azure ad client secret(%s)", LauncherConfig.Web.getAuzureADClientSecret()));
			Logger.logInfo(Launcher.this, String.format("config: web azure ad login redirect url(%s)", LauncherConfig.Web.getAuzureADLoginRedirectURL()));
			handler.handle(Future.succeededFuture());
		} catch (Exception e) {
			handler.handle(Future.failedFuture(e));
		}
	}
	
	protected void deployVerticles(Handler<AsyncResult<Void>> handler)
	{
		try {
			this.vertx.deployVerticle(WebServerVerticle.class.getName(), ar -> {
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
}
