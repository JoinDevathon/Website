package net.burngames.devathon.routes;

import net.burngames.devathon.Website;
import net.burngames.devathon.persistence.Sessions;
import net.burngames.devathon.routes.auth.AccountInfo;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @author PaulBGD
 */
public class AccountRoute implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request request, Response response) throws Exception {
        Sessions sessions = Website.getSessions();
        String token = sessions.init(request, response);
        Sessions.SessionObject session = sessions.getSession(token);
        if (session == null || !session.json.has("id")) {
            throw new RouteException("You are not logged in.");
        }
        int id = session.json.getInt("id");
        final AccountInfo info = Website.getUserDatabase().getUserById(id);
        if (info == null) {
            throw new RouteException("An error occurred loading account info.");
        }
        Map<String, Object> map = new HashMap<>(info.toMap());
        Object email = map.get("email");
        if (email == null || !(email instanceof String) || !((String) email).contains("@")) {
            map.put("error", "You haven't entered an email yet! In order to participate you must have a working email.");
        }
        return new ModelAndView(map, "account.mustache");
    }
}
