package server;

public enum Option {

    LOGIN("/login"),
    WHOAMI("/whoami"),
    WHO("/who"),
    CHAT("/chat"),
    TO("/to"),
    EXIT("/exit");

    public String name;

    Option(String name) {
        this.name = name;
    }
}
