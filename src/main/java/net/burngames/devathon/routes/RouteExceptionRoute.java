package net.burngames.devathon.routes;

import spark.ExceptionHandler;
import spark.Request;
import spark.Response;

/**
 * @author PaulBGD
 */
public class RouteExceptionRoute implements ExceptionHandler {
    @Override
    public void handle(Exception e, Request request, Response response) {
        response.body(e.getMessage());
    }
}
