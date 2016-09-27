package net.burngames.devathon.routes;

import net.burngames.devathon.Website;
import net.burngames.devathon.persistence.Sessions;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * @author PaulBGD
 */
public class AccountUpdateRoute implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        Sessions sessions = Website.getSessions();
        String token = sessions.init(request, response);
        Sessions.SessionObject session = sessions.getSession(token);
        if (session == null || !session.json.has("id")) {
            throw new RouteException("You are not logged in.");
        }
        int id = session.json.getInt("id");
        String service = request.queryParams("service");
        String value = request.queryParams("value");

        if (value == null || service == null || value.length() > 30) {
            return "failed";
        }

        switch (service) {
            case "beam":
                Website.getUserDatabase().setBeam(id, value);
                break;
            case "twitch":
                Website.getUserDatabase().setTwitch(id, value);
                break;
            case "twitter":
                Website.getUserDatabase().setTwitter(id, value);
                break;
            default:
                return "failed";
        }

        return "success";
    }
}
