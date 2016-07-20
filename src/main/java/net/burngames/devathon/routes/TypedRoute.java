package net.burngames.devathon.routes;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Lazy typesafe extension of {@link Route}
 * 
 * @param <T> type of the object to return in {@link TypedRoute#handleTyped}
 * 
 * @author Kenny
 */
public interface TypedRoute<T> extends Route
{

    T handleTyped (Request quest, Response response) throws Exception;
    
    @Override
    default Object handle(Request request, Response response) throws Exception 
    {
        return (T) handleTyped(request, response);
    }
    
}
