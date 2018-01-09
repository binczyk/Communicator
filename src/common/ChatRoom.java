package common;

import java.util.LinkedList;

public class ChatRoom {

    private String roomName;
    private LinkedList<Integer> members = new LinkedList<>();
    private int ower;

    public ChatRoom(String roomName, int ower) {
        this.roomName = roomName;
        this.ower = ower;
        addMember(ower);
    }

    public ChatRoom(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomName() {
        return roomName;
    }

    public LinkedList<Integer> getMembers() {
        return members;
    }

    public void addMember(int userId) {
        members.add(userId);
    }

    public void singOut(int userId) {
        members.remove(userId);
    }

}
