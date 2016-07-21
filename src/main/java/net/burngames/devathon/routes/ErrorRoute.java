package net.burngames.devathon.routes;

import com.google.common.collect.ImmutableMap;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

/**
 * @author PaulBGD
 */
public class ErrorRoute implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request request, Response response) throws Exception {
        String message = "An unknown error occurred.";
        if (request.queryParams().contains("message")) {
            message = request.queryParams("message");
        }
        return new ModelAndView(ImmutableMap.of(
                "message", message
        ), "error.mustache");
    }
}
