package net.burngames.devathon.routes;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Lazy typesafe extension of {@link Route}
 *
 * @param <T> type of the object to return in {@link TypedRoute#handleTyped}
 * @author Kenny
 */
public interface TypedRoute<T> extends Route {

    /**
     * Invoked when a request is made on this route's corresponding path e.g. '/hello'
     *
     * @param request  The request object providing information about the HTTP request
     * @param response The response object providing functionality for modifying the response
     * @return The content to be set in the response
     * @throws java.lang.Exception implementation can choose to throw exception
     */
    T handleTyped(Request quest, Response response) throws Exception;

    @Override
    default Object handle(Request request, Response response) throws Exception {
        return (T) handleTyped(request, response);
    }

}
