package app.myfood;

import com.google.firebase.storage.StorageReference;

/**
 * Created by HP on 5/29/2017.
 */

public class ItemTimeline{
    String user_id;
    String id;
    String title;
    StorageReference image;
    long like_count, comment_count;

    public ItemTimeline(String user_id, String id, String title, StorageReference image, long comment_count, long like_count) {
        this.comment_count = comment_count;
        this.id = id;
        this.image = image;
        this.like_count = like_count;
        this.title = title;
        this.user_id = user_id;
    }
}
