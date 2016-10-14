package net.burngames.devathon.routes.trophy;

/**
 * @author PaulBGD
 */
public class Trophy {

    public static final Trophy EARLY_SIGNUP = new Trophy("earlysignup2016", "Signed Up Early");
    public static final Trophy PARTICIPATED2016 = new Trophy("participated2016", "Participated in 2016");
    public static final Trophy TWEETED = new Trophy("tweeted", "Tweeted About the Contest");
    public static final Trophy WINNER2016 = new Trophy("winner2016", "Won the 2016 Contest");

    /**
     * The name of the file without .svg (ex earlysignup2016)
     */
    private final String trophy;
    /**
     * The name to be listed with the trophy
     */
    private final String name;

    private Trophy(String trophy, String name) {
        this.trophy = trophy;
        this.name = name;
    }

    public String getTrophy() {
        return trophy;
    }

    public String getName() {
        return name;
    }
}
