package Model;

public class Notifications {
    private String userid;
    private  String text;
    private String postid;
    private  boolean isPost;

    public Notifications(String userid, String text, String postid, boolean isPost) {
        this.userid = userid;
        this.text = text;
        this.postid = postid;
        this.isPost = isPost;
    }

    public Notifications() {
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public boolean isPost() {
        return isPost;
    }

    public void setPost(boolean post) {
        isPost = post;
    }
}
