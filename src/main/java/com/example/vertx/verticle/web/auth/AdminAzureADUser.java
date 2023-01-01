package com.example.vertx.verticle.web.auth;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;

public class AdminAzureADUser extends AbstractUser
{
	public AdminFormLoginAuthProvider auth_provider;
	public JsonObject principal;
	public String username;
	public String access_token;
	public String id_token;
	
	public AdminAzureADUser(String username, String access_token, String id_token)
	{
		this.username = username;
		this.access_token = access_token;
		this.id_token = id_token;
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
											 .put("access_token", this.access_token)
											 .put("id_token", this.id_token);
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