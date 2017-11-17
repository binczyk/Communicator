package server;

public enum Option {

    LOGIN("/login"),
    WHOAMI("/whoami"),
    WHO("/who"),
    CHAT("/chat"),
    TO("/to"),
    EXIT("/exit");

    private static String name;

    public String getName() {
        return name;
    }

}
