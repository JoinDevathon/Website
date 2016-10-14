package net.burngames.devathon.routes;

import net.burngames.devathon.Website;
import net.burngames.devathon.persistence.Sessions;
import net.burngames.devathon.routes.auth.AccountInfo;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

import java.util.*;

/**
 * @author PaulBGD
 */
public class AccountRoute implements TemplateViewRoute {

    public static class SimpleTrophy {
        public String trophy;
        public String name;

        public SimpleTrophy(String trophy, String name) {
            this.trophy = trophy;
            this.name = name;
        }
    }

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
        List<SimpleTrophy> trophyList = new ArrayList<>(info.getTrophies().size());
        for (Map.Entry<String, String> entry : info.getTrophies().entrySet()) {
            trophyList.add(new SimpleTrophy(entry.getKey(), entry.getValue()));
        }
        if(trophyList.isEmpty()) {
            map.remove("trophies");
        } else {
            map.put("trophies", trophyList);
        }
        return new ModelAndView(map, "account.mustache");
    }
}
