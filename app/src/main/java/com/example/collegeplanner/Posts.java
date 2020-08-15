package com.example.collegeplanner;

public class Posts {
    public String userId, userName, userDp, postDescription, postImage, postTime, postId, postComments;

    public Posts(){

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserDp() {
        return userDp;
    }

    public void setUserDp(String userDp) {
        this.userDp = userDp;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostComments() {
        return postComments;
    }

    public void setPostComments(String postComments) {
        this.postComments = postComments;
    }

    public Posts(String userId, String userName, String userDp, String postDescription, String postImage, String postTime, String postId, String postComments) {
        this.userId = userId;
        this.userName = userName;
        this.userDp = userDp;
        this.postDescription = postDescription;
        this.postImage = postImage;
        this.postTime = postTime;
        this.postId = postId;
        this.postComments = postComments;
    }
}
