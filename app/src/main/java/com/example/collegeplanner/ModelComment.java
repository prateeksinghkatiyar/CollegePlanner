package com.example.collegeplanner;

public class ModelComment {
    String comment, commentId, commentTime, userDp, userId, uEmail, userName;

    public ModelComment() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(String commentTime) {
        this.commentTime = commentTime;
    }

    public String getUserDp() {
        return userDp;
    }

    public void setUserDp(String userDp) {
        this.userDp = userDp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ModelComment(String comment, String commentId, String commentTime, String userDp, String userId, String uEmail, String userName) {
        this.comment = comment;
        this.commentId = commentId;
        this.commentTime = commentTime;
        this.userDp = userDp;
        this.userId = userId;
        this.uEmail = uEmail;
        this.userName = userName;


    }
}
