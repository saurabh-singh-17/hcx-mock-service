package org.swasth.hcx.dto;

public class Notification {
    private String sender_code;
    private String recipient_code;
    private String read;
    private String created_on;
    private String request_id;
    private String topic_code;
    private String message;

    public String getSender_code() {
        return sender_code;
    }

    public void setSender_code(String sender_code) {
        this.sender_code = sender_code;
    }

    public String getRecipient_code() {
        return recipient_code;
    }

    public void setRecipient_code(String recipient_code) {
        this.recipient_code = recipient_code;
    }

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }

    public String getCreated_on() {
        return created_on;
    }

    public void setCreated_on(String created_on) {
        this.created_on = created_on;
    }

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public String getTopic_code() {
        return topic_code;
    }

    public void setTopic_code(String topic_code) {
        this.topic_code = topic_code;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }



    // Constructors, getters, and setters

    public Notification() {}

    public Notification(String request_id, String message, String topic_code, String sender_code, String recipient_code, String read, String created_on) {
        this.request_id = request_id;
        this.message = message;
        this.topic_code = topic_code;
        this.sender_code = sender_code;
        this.recipient_code = recipient_code;
        this.read = read;
        this.created_on = created_on;
    }

}

