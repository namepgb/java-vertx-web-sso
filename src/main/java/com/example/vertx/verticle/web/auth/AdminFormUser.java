package com.example.vertx.verticle.web.auth;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;

public class AdminFormUser extends AbstractUser
{
	public AdminFormLoginAuthProvider auth_provider;
	public JsonObject principal;
	public int id;
	public String username;
	public String password;
	
	public AdminFormUser(String username, String password)
	{
		this.username = username;
		this.password = password;
	}
	
	@Override
	protected void doIsPermitted(String permission, Handler<AsyncResult<Boolean>> handler)
	{
		handler.handle(Future.succeededFuture(true));
	}
	
	@Override
	public JsonObject principal()
	{
		if (this.principal == null)
			this.principal = new JsonObject().put("username", this.username)
											 .put("password", this.password);
		return this.principal;
	}
	
	@Override
	public void setAuthProvider(AuthProvider authProvider)
	{
		if (auth_provider instanceof AdminFormLoginAuthProvider)
			this.auth_provider = (AdminFormLoginAuthProvider) authProvider;
		else
			throw new IllegalArgumentException();
	}
}
