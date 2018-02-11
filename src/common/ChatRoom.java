package common;

import java.util.LinkedList;

public class ChatRoom {

    private int id;
    private String roomName;
    private LinkedList<Integer> members = new LinkedList<>();
    private int owner;

    public ChatRoom(String roomName, int owner) {
        this.roomName = roomName;
        this.owner = owner;
        addMember(owner);
    }

    public ChatRoom(int id, String roomName, int owner) {
        this(roomName, owner);
        this.id = id;
    }

    public String getRoomName() {
        return roomName;
    }

    public LinkedList<Integer> getMembers() {
        return members;
    }

    public int getOwner() {
        return owner;
    }

    public void addMember(int userId) {
        if (!members.contains(userId)) {
            members.add(userId);
        }
    }

    public void singOut(int userId) {
        members.remove(userId);
    }

    public int getId() {
        return id;
    }

    public void setMembers(LinkedList<Integer> members) {
        this.members = members;
    }
}
