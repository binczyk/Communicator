package server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Chat {

    private Map<String, HashSet<Server>> members = new HashMap<>();


    public Map<String, HashSet<Server>> getMembers() {
        return members;
    }

    public void setMembers(Map<String, HashSet<Server>> members) {
        this.members = members;
    }

    public void init(String chatName, Server server) {
        HashSet<Server> initList = new HashSet<>();
        initList.add(server);
        this.members.put(chatName, initList);
    }

    public void addMembersToChat(String chatName, Server memeberServer) {
        members.get(chatName).add(memeberServer);
    }
}
