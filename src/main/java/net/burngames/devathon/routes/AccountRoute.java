package net.burngames.devathon.routes;

import com.google.common.collect.ImmutableMap;
import net.burngames.devathon.Website;
import net.burngames.devathon.persistence.Sessions;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

/**
 * @author PaulBGD
 */
public class AccountRoute implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request request, Response response) throws Exception {
        Sessions sessions = Website.getSessions();
        String token = sessions.init(request);
        Sessions.SessionObject session = sessions.getSession(token);
        if (!session.json.has("id")) {
            throw new RouteException("You are not logged in.");
        }
        int id = session.json.getInt("id");
        return new ModelAndView(ImmutableMap.of(
                "id", id
        ), "account.mustache");
    }
}
