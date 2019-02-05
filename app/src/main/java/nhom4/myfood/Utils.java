package nhom4.myfood;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by HP on 5/29/2017.
 */

public class Utils {

    public static int getValueType(Notification_Type type)
    {
        switch (type)
        {
            case LIKE: return 0;
            case COMMENT: return 1;
            case NEW_POST: return 2;
            case FOLLOW: return 3;
        }
        return -1;
    }

    public static Notification_Type getType(int i)
    {
        switch (i)
        {
            case 0: return Notification_Type.LIKE;
            case 1: return Notification_Type.COMMENT;
            case 2: return Notification_Type.NEW_POST;
            case 3: return Notification_Type.FOLLOW;
        }
        return null;
    }

    public static void pushNotification(String user_id, String post_id, String date, String uid, String fullname, String photo, ArrayList<String> followed_by, Notification_Type type)
    {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("post_id", post_id);
        hashMap.put("date", date);
        hashMap.put("fullname", fullname);
        hashMap.put("photo", photo);
        hashMap.put("type", getValueType(type));
        hashMap.put("user_id", uid);
        hashMap.put("pushed", 0);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        if(type == Notification_Type.NEW_POST)
        {
            hashMap.put("user_id", uid);
            for(String user: followed_by)
            {
                String key = databaseReference.child(user).child("notifications").push().getKey().toString();
                databaseReference.child(user).child("notifications").child(key).setValue(hashMap);
            }
        }
        else
        {
            String key = databaseReference.child(user_id).child("notifications").push().getKey().toString();
            databaseReference.child(user_id).child("notifications").child(key).setValue(hashMap);
        }
    }

    public static HashMap<String, Object> getProfile()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final HashMap<String, Object> hashMap = new HashMap<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String fullname = dataSnapshot.child("fullname").getValue().toString();
                String photo = dataSnapshot.child("photo").getValue().toString();

                ArrayList<String> follows = new ArrayList<String>();

                for(DataSnapshot item: dataSnapshot.getChildren())
                    follows.add(item.getKey().toString());

                hashMap.put("fullname", fullname);
                hashMap.put("photo", photo);
                hashMap.put("followed_by", follows);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return hashMap;
    }

    public static String getFileName(ContentResolver contentResolver, Uri uri)
    {
        String filename = null;

        if(uri.getScheme().equals("content")) {
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }

        if(filename == null)
        {
            int index = uri.getPath().lastIndexOf("/") + 1;
            filename = uri.getPath().substring(index, uri.getPath().length());
        }

        return filename;
    }
}
