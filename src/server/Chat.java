package server;

import java.util.HashSet;

public class Chat {

    private String name;
    private HashSet<Server> members = new HashSet<>();


    public Chat(String roomName, Server server) {
        this.members.add(server);
        this.name = roomName;
    }

    public void addMember(Server memeber) {
        this.members.add(memeber);
    }

    public HashSet<Server> getMembers() {
        return members;
    }

    public void setMembers(HashSet members) {
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
