package nhom4.myfood;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

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
