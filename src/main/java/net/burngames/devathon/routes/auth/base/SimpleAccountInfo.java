package net.burngames.devathon.routes.auth.base;

import com.google.common.collect.ImmutableMap;
import net.burngames.devathon.routes.auth.AccountInfo;

import java.util.Map;

/**
 * Basic implementation of {@link AccountInfo}.
 *
 * @author Kenny
 */
public class SimpleAccountInfo implements AccountInfo {

    private final int id;

    private final String username;
    private final String email;
    private final String beam;
    private final String twitch;
    private final String twitter;

    public SimpleAccountInfo(int id, String username, String email, String beam, String twitch, String twitter) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.beam = beam;
        this.twitch = twitch;
        this.twitter = twitter;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public Map<String, Object> toMap() {
        return ImmutableMap.<String, Object>builder()
                .put("id", this.id)
                .put("username", this.username)
                .put("email", this.email)
                .put("beam", this.beam)
                .put("twitch", this.twitch)
                .put("twitter", this.twitter)
                .build();
    }

    @Override
    public int getId() {
        return id;
    }

    public String getBeam() {
        return beam;
    }

    public String getTwitch() {
        return twitch;
    }

    public String getTwitter() {
        return twitter;
    }

    @Override
    public String toString() {
        return username + "( " + id + ", " + ", " + email + " )";
    }

}
