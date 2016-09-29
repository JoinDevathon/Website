package net.burngames.devathon.routes;

import com.google.common.base.Joiner;
import net.burngames.devathon.Website;
import net.burngames.devathon.persistence.Sessions;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author PaulBGD
 */
public class AccountDetailsRoute implements Route {

    private static final Pattern pattern = Pattern.compile("^.+@.+\\..+$");

    @Override
    public Object handle(Request request, Response response) throws Exception {
        Sessions sessions = Website.getSessions();
        String token = sessions.init(request, response);
        Sessions.SessionObject session = sessions.getSession(token);
        if (session == null || !session.json.has("id")) {
            throw new RouteException("You are not logged in.");
        }

        int id = session.json.getInt("id");
        if (!request.queryParams().contains("email")) {
            throw new RouteException("Email is not supplied!");
        }
        Matcher matcher = pattern.matcher(request.queryParams("email"));
        if (!matcher.matches()) {
            throw new RouteException("Bad email address provided.");
        }
        Website.getUserDatabase().setEmail(id, request.queryParams("email"));
        response.redirect("/account");
        return "";
    }
}
