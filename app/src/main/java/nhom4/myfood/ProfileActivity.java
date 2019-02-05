package nhom4.myfood;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    HashMap<String, ArrayList<ItemTimeline>> list_date = new HashMap<>();
    TimelineAdapter adapter;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("users");
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

    TextView txtName, txtPicture, txtFollow, txtPost;
    CircleImageView circleImageView;

    String user_id = "";

    boolean isFollowed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);

        user_id = getIntent().getStringExtra("user_id");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Thông tin");

        final Button btnFollow = (Button) findViewById(R.id.btnFollow);

        databaseReference.child(user_id).child("followed_by").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(user.getUid()))
                {
                    isFollowed = true;
                    btnFollow.setText("Bỏ theo dõi");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(user_id.equals(user.getUid()))
        {
            btnFollow.setVisibility(View.GONE);
        }
        else {
            btnFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!isFollowed) {
                        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        Date dt = new Date();
                        final String date = df.format(dt);
                        databaseReference.child(user_id).child("followed_by").child(user.getUid()).setValue(date).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                isFollowed = true;
                                btnFollow.setText("Bỏ theo dõi");
                                databaseReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Utils.pushNotification(user_id, "", date, user.getUid(), dataSnapshot.child("fullname").getValue().toString(), dataSnapshot.child("photo").getValue().toString(), null, Notification_Type.FOLLOW);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                databaseReference.child(user.getUid()).child("follow").child(user_id).setValue(date);
                            }
                        });
                    } else {
                        databaseReference.child(user_id).child("followed_by").child(user.getUid()).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                isFollowed = false;
                                btnFollow.setText("Theo dõi");
                                databaseReference.child(user.getUid()).child("follow").child(user_id).removeValue();
                            }
                        });
                    }
                }
            });
        }

        txtName = (TextView) findViewById(R.id.txtName);
        txtFollow = (TextView) findViewById(R.id.txtFollow);
        txtPicture = (TextView) findViewById(R.id.txtPicture);
        txtPost = (TextView) findViewById(R.id.txtPost);
        circleImageView = (CircleImageView) findViewById(R.id.image);

        adapter = new TimelineAdapter(getApplicationContext(), R.layout.timeline_layout, list_date);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.timelineRecycleView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(user_id))
                {
                    Toast.makeText(getApplicationContext(), "Tài khoản này đã bị xoá", Toast.LENGTH_LONG).show();
                    finish();
                }

                databaseReference.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String photo = dataSnapshot.child("photo").getValue().toString();
                        StorageReference sr_photo = storageReference.child(user_id).child(photo);

                        Glide.with(getApplicationContext()).using(new FirebaseImageLoader()).load(sr_photo).into(circleImageView);

                        txtName.setText(dataSnapshot.child("fullname").getValue().toString());
                        txtPost.setText(dataSnapshot.child("posts").getChildrenCount() + " bài viết");
                        txtFollow.setText(dataSnapshot.child("followed_by").getChildrenCount() + " lượt theo dõi");

                        long images = 0;

                        for(DataSnapshot post: dataSnapshot.child("posts").getChildren())
                        {
                            images += post.child("images").getValue().toString().split(";").length;
                        }

                        txtPicture.setText(images + " tấm hình");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                GetAllPost(user_id);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    void GetAllPost(final String user_id)
    {
        DatabaseReference db_posts = databaseReference.child(user_id).child("posts");
        db_posts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!list_date.isEmpty()) list_date.clear();

                DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

                for(DataSnapshot post: dataSnapshot.getChildren())
                {
                    String id = post.getKey();
                    String title = post.child("title").getValue().toString();
                    String post_date = post.child("created").getValue().toString();
                    String image = post.child("images").getValue().toString().split(";")[0];
                    long like = post.child("like").getChildrenCount();
                    long cm = post.child("comments").getChildrenCount();

                    try {
                        String date = df.format(df.parse(post_date));

                        if(!list_date.containsKey(date))
                        {
                            list_date.put(date, new ArrayList<ItemTimeline>());
                        }

                        list_date.get(date).add(new ItemTimeline(user_id, id, title, storageReference.child(user_id).child(id).child(image), cm, like));

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
