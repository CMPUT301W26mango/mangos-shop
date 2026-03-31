package com.example.myapplication;
import com.google.firebase.Timestamp;

/**
 * This is a comment class it represents one comment
 * It stores the text, date, who made the comment on which device, and when
 * */
public class Comment {
    private String commentText;
    private String userName;
    private String deviceId;
    private Timestamp timestamp;

    /**
     * Empty constructor
     * */
    public Comment(){}

    /**
     * Constructor for the Comment class. This initializes all the variables
     * @param commentText
     *  This is the text for the comment, this is the actual content
     * @param deviceId
     *  This is the ID of the android device that made the comment
     * @param timestamp
     *  This is the time that the comment was written
     * @param userName
     *  This is the name of the user that made the comment
     * */
    public Comment(String commentText, String userName, String deviceId, Timestamp timestamp) {
        this.commentText = commentText;
        this.userName = userName;
        this.deviceId = deviceId;
        this.timestamp = timestamp;
    }

    /**
     * This returns the text of the comment, i.e the content
     * @return commentText
     *  This is the content of the comment
     *
     * */
    public String getCommentText() {
        return commentText;
    }

    /**
     *  This sets the comment content
     * @param commentText
     *  The content is passed in
     * */
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    /**
     * This returns the user who made the comment
     * @return userName
     *  This is the name of the user that made the comment
     * */
    public String getUserName() {
        return userName;
    }

    /**
     * This sets the person who made the comment
     * @param userName
     *  The name of the user is passed in
     * */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * This returns the id of the device that made the comment
     * @return deviceId
     *  This is the id of the device
     * */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * This sets the Id of the device that made the comment
     * @param deviceId
     *  Passed in Id of the device that made the comment
     * */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * This returns the time that the comment was written
     * @return timestamp
     *  The time that the comment was written
     * */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * This sets the time that the comment was written
     * @param timestamp
     *  This is passed in and is a timestamp of the time that the comment was written
     * */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
