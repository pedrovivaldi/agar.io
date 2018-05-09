package server;

import java.io.Serializable;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Pedro Vivaldi
 */
public class Message implements Serializable {

    private Object content;
    private Object content2;
    private MessageType type;

    public Message(Object content, Object content2, MessageType type) {
        this.content = content;
        this.content2 = content2;
        this.type = type;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public Object getContent2() {
        return content2;
    }

    public void setContent2(Object content2) {
        this.content2 = content2;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

}
