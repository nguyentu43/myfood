package nhom4.myfood;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ThongBaoFragment extends Fragment {
    ArrayList<ThongBao> list = new ArrayList<>();
    ThongBaoItemAdapter adapter;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("notifications");
    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("users");

    public ThongBaoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_thong_bao, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {

                final Notification_Type type = Utils.getType(Integer.parseInt(dataSnapshot.child("type").getValue().toString()));
                final String user_id = dataSnapshot.child("user_id").getValue().toString();


                FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot data) {

                        if(!data.hasChild(user_id)) return;

                        String content = "<strong>" + dataSnapshot.child("fullname").getValue().toString() + "</strong>";
                        String photo = dataSnapshot.child("photo").getValue().toString();
                        String date = dataSnapshot.child("date").getValue().toString();
                        String post_id = dataSnapshot.child("post_id").getValue().toString();

                        switch (type)
                        {
                            case LIKE: content+= " đã thích bài viết của bạn"; break;
                            case COMMENT: content += " đã bình luận bài viết của bạn"; break;
                            case NEW_POST: content += " đã đăng bài viết mới"; break;
                            case FOLLOW: content += " đã theo dõi bạn"; break;
                        }

                        ThongBao tb = new ThongBao(content, date, storageReference.child(user_id).child(photo), user_id, post_id, type);

                        list.add(0, tb);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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
        });

        ListView listView = (ListView) getView().findViewById(R.id.listView);
        adapter = new ThongBaoItemAdapter(getContext(), R.layout.thong_bao_item, list);
        listView.setAdapter(adapter);

    }

    public class ThongBao{
        String info;
        String time;
        StorageReference photo;
        String user_id;
        String post_id;
        Notification_Type type;

        public ThongBao(String info, String time, StorageReference photo, String user_id, String post_id, Notification_Type type) {
            this.photo = photo;
            this.info = info;
            this.time = time;
            this.user_id = user_id;
            this.post_id = post_id;
            this.type = type;
        }
    }

    public class ThongBaoItemAdapter extends ArrayAdapter<ThongBao>{

        LayoutInflater inflater;
        int layoutId;
        ArrayList<ThongBao> list;

        public ThongBaoItemAdapter(Context context, int resource, ArrayList<ThongBao> objects) {
            super(context, resource, objects);
            inflater = LayoutInflater.from(context);
            this.layoutId = resource;
            list = objects;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if(convertView == null)
                v = inflater.inflate(layoutId, null);

            ThongBao tb = list.get(position);

            TextView txtInfo = (TextView) v.findViewById(R.id.txtInfo);
            txtInfo.setText(Html.fromHtml(tb.info));
            TextView txtTime = (TextView) v.findViewById(R.id.txtTime);
            txtTime.setText(tb.time);
            CircleImageView imageView = (CircleImageView) v.findViewById(R.id.imageAvatar);
            Glide.with(getContext()).using(new FirebaseImageLoader()).load(tb.photo).into(imageView);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ThongBao tb = list.get(position);
                    if(tb.type != Notification_Type.FOLLOW)
                    {
                        Intent intent = new Intent(getContext(), BaiVietActivity.class);
                        intent.putExtra("user_id", user.getUid());
                        intent.putExtra("id", tb.post_id);
                        startActivity(intent);
                    }
                }
            });

            return v;
        }
    }
}
