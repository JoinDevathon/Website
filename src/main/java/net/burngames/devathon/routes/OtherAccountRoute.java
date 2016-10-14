package net.burngames.devathon.routes;

import net.burngames.devathon.Website;
import net.burngames.devathon.routes.auth.AccountInfo;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author PaulBGD
 */
public class OtherAccountRoute implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request request, Response response) throws Exception {
        final AccountInfo info = Website.getUserDatabase().getUserByUsername(request.params("user"));
        if (info == null) {
            throw new RouteException("An error occurred loading account info.");
        }
        if (info.getId() == -1) {
            throw new RouteException("That user does not exist!");
        }
        Map<String, Object> map = new HashMap<>(info.toMap());
        List<AccountRoute.SimpleTrophy> trophyList = new ArrayList<>(info.getTrophies().size());
        for (Map.Entry<String, String> entry : info.getTrophies().entrySet()) {
            trophyList.add(new AccountRoute.SimpleTrophy(entry.getKey(), entry.getValue()));
        }
        if(trophyList.isEmpty()) {
            map.remove("trophies");
        } else {
            map.put("trophies", trophyList);
        }
        map.put("readonly", true);
        return new ModelAndView(map, "account.mustache");
    }
}
