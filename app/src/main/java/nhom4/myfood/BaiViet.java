package nhom4.myfood;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by HP on 4/26/2017.
 */

public class BaiViet {
    String id, user_id;
    String content, title;
    String post_name;
    StorageReference imageAvatar;
    List<StorageReference> image;
    String postTime;
    String modified;
    long like_count;
    long cm_count;
    boolean liked = false;

    public BaiViet(StorageReference imageAvatar, List<StorageReference> image, String post_name, String title, String content, String user_id, String id, String date, String modified, long n, long cm) {
        this.id = id;
        this.imageAvatar = imageAvatar;
        this.image = image;
        this.title = title;
        this.content = content;
        this.postTime = date;
        this.modified = modified;
        this.user_id = user_id;
        this.like_count = n;
        this.cm_count = cm;
        this.post_name = post_name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<StorageReference> getImage() {
        return image;
    }

    public void setImage(List<StorageReference> image) {
        this.image = image;
    }

    public StorageReference getImageAvatar() {
        return imageAvatar;
    }

    public void setImageAvatar(StorageReference imageAvatar) {
        this.imageAvatar = imageAvatar;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public long getLike_count() {
        return like_count;
    }

    public long getCm_count() {
        return cm_count;
    }

    public void setLike_count(long like_count) {
        this.like_count = like_count;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public String getPost_name() {
        return post_name;
    }

    public void setPost_name(String post_name) {
        this.post_name = post_name;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }
}
