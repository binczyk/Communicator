/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.sql.Timestamp;

/**
 * @author mariusz
 */
public class Message {
    private int id;
    private Timestamp sent;
    private Timestamp read;
    private int from;
    private int to;
    private String content;
    private String type;

    public Message(Timestamp sent, Timestamp read, int from, int to, String content) {
        this.sent = sent;
        this.read = read;
        this.from = from;
        this.to = to;
        this.content = content;
    }

    public Message(int id, Timestamp sent, Timestamp read, int from, int to, String content) {
        this(sent, read, from, to, content);
        this.id = id;
    }

    public Message(int id, Timestamp sent, Timestamp read, int from, int to, String content, String type) {
        this(id, sent, read, from, to, content);
        this.type = type;
    }

    public Message(Timestamp sent, Timestamp read, int from, int to, String content, String type) {
        this(sent, read, from, to, content);
        this.type = type;
    }

    public Timestamp getSent() {
        return sent;
    }

    public Timestamp getRead() {
        return read;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public String getContent() {
        return content;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }
};