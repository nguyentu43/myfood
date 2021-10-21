package app.myfood;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class NotificationService extends Service {

    NotificationCompat.Builder builder;

    private static final int REQUEST_CODE = 20;

    public NotificationService() {
    }

    DatabaseReference databaseReference;

    ChildEventListener childEventListener;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        builder = new NotificationCompat.Builder(this);

        builder.setAutoCancel(true);

        builder.setSmallIcon(R.mipmap.ic_launcher);

        builder.setContentTitle("Bạn có thông báo mới");

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if(dataSnapshot.child("pushed").getValue().toString().equals("1"))
                    return;
                String content = dataSnapshot.child("fullname").getValue().toString();

                int type = Integer.parseInt(dataSnapshot.child("type").getValue().toString());

                switch (Utils.getType(type)) {
                    case LIKE:
                        content += " đã thích một bài viết của bạn";
                        break;
                    case COMMENT:
                        content += " đã bình luận về bài viết của bạn";
                        break;
                    case NEW_POST:
                        content += " có bài viết mới";
                        break;
                    case FOLLOW:
                        content += " theo dõi bạn";
                }

                builder.setContentText(content);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                builder.setContentIntent(pendingIntent);

                final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                final Notification notification = builder.build();

                final int id = (int) ((new Date().getTime()/1000L) % Integer.MAX_VALUE);

                databaseReference.child(dataSnapshot.getKey().toString()).child("pushed").setValue("1").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        notificationManager.notify(id, notification);
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
        {
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("notifications");
            databaseReference.addChildEventListener(childEventListener);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(childEventListener);
    }
}
