package com.example.vertx.verticle.web.auth;

import com.example.vertx.verticle.launcher.LauncherConfig;
import com.example.vertx.verticle.web.WebServerVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

public class AdminFormLoginAuthProvider implements AuthProvider
{
	protected WebServerVerticle verticle;
	
	public AdminFormLoginAuthProvider(WebServerVerticle verticle)
	{
		this.verticle = verticle;
	}
	
	@Override
	public void authenticate(JsonObject credentials, Handler<AsyncResult<User>> handler)
	{
		String username = credentials.getString("username");
		String password = credentials.getString("password");
		
		// admin 계정 ID가 일치하지 않음
		if (0 != username.compareTo(LauncherConfig.Web.getAdminUserName())) {
			handler.handle(Future.failedFuture("invalid admin username"));
			return;
		}
		// admin 계정 PW가 일치하지 않음
		if (0 != password.compareTo(LauncherConfig.Web.getAdminPassword())) {
			handler.handle(Future.failedFuture("invalid admin password"));
			return;
		}
		
		AdminFormUser admin_user = new AdminFormUser(username, password);
		handler.handle(Future.succeededFuture(admin_user));
	}
}
