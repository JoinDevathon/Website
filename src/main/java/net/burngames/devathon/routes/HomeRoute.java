package net.burngames.devathon.routes;

import com.google.common.collect.ImmutableMap;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

/**
 * @author PaulBGD
 */
public class HomeRoute implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request request, Response response) throws Exception {
        return new ModelAndView(ImmutableMap.of(), "home.mustache");
    }
}
