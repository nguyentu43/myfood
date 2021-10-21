package app.myfood;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class CaNhanFragment extends Fragment {

    HashMap<String, ArrayList<ItemTimeline>> list_date = new HashMap<>();
    TimelineAdapter adapter;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("users");
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

    TextView txtName, txtPicture, txtFollow, txtPost;
    CircleImageView circleImageView;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        txtName = (TextView) getView().findViewById(R.id.txtName);
        txtFollow = (TextView) getView().findViewById(R.id.txtFollow);
        txtPicture = (TextView) getView().findViewById(R.id.txtPicture);
        txtPost = (TextView) getView().findViewById(R.id.txtPost);
        circleImageView = (CircleImageView) getView().findViewById(R.id.image);

        txtFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListFollowDialog dialog = new ListFollowDialog(getContext());
                dialog.setContentView(R.layout.list_user);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        Button btnFollow = (Button) getView().findViewById(R.id.btnFollow);
        btnFollow.setVisibility(View.GONE);

        adapter = new TimelineAdapter(getContext(), R.layout.timeline_layout, list_date);
        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.timelineRecycleView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);

        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GetInfo();
                GetAllPost(user.getUid());
            }
        });

        GetInfo();
        GetAllPost(user.getUid());
    }

    void GetInfo()
    {
        databaseReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String photo = dataSnapshot.child("photo").getValue().toString();
                StorageReference sr_photo = storageReference.child(user.getUid()).child(photo);

                Glide.with(getContext()).using(new FirebaseImageLoader()).load(sr_photo).into(circleImageView);

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
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
