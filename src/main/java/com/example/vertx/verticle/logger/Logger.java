package com.example.vertx.verticle.logger;

import com.example.vertx.verticle.WrappedAbstractVerticle;
import io.vertx.core.logging.LoggerFactory;

public class Logger
{
	protected static io.vertx.core.logging.Logger LOGGER = LoggerFactory.getLogger(WrappedAbstractVerticle.class);
	
	public static void logInfo(WrappedAbstractVerticle verticle, String message)
	{
		String log = String.format("[%s] %s", verticle.getVerticleName(), message);
		LOGGER.info(log);
	}
	
	public static void logError(WrappedAbstractVerticle verticle, Throwable cause)
	{
		String log = String.format("[%s] %s", verticle.getVerticleName(), cause.getMessage());
		LOGGER.error(log);
	}
	
	public static void logError(WrappedAbstractVerticle verticle, String message)
	{
		String log = String.format("[%s] %s", verticle.getVerticleName(), message);
		LOGGER.error(log);
	}
	
	public static void logError(WrappedAbstractVerticle verticle, String message, Throwable cause)
	{
		String log = String.format("[%s] %s\n%s", verticle.getVerticleName(), message, cause.getMessage());
		LOGGER.error(log);
	}
}
