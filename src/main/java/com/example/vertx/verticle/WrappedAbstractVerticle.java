package com.example.vertx.verticle;

import com.example.vertx.verticle.logger.Logger;
import io.vertx.core.*;

public abstract class WrappedAbstractVerticle extends AbstractVerticle
{
	protected String verticle_name = this.getClass().getSimpleName();
	
	@Override
	public void start(Future<Void> future) throws Exception
	{
		start();
		onStarted(ar -> {
			if (ar.failed()) {
				Logger.logError(this, ar.cause());
				future.fail(ar.cause());
			} else {
				Logger.logInfo(this, "verticle had been deploymented");
				future.complete();
			}
		});
	}
	
	public String getVerticleName()
	{
		return this.verticle_name;
	}
	
	protected abstract void onStarted(Handler<AsyncResult<Void>> handler);
}