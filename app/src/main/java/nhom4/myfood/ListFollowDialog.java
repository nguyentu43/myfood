package nhom4.myfood;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by HP on 6/1/2017.
 */

public class ListFollowDialog extends Dialog {

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("users");
    ArrayList<User> userList = new ArrayList<>();

    UserAdapter adapter;

    public ListFollowDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseReference.child(user.getUid()).child("followed_by").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    databaseReference.child(item.getKey().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            userList.add(new User(dataSnapshot.child("fullname").getValue().toString(), dataSnapshot.child("photo").getValue().toString(), dataSnapshot.getKey().toString()));
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        ListView listView = (ListView) findViewById(R.id.listView);
        adapter = new UserAdapter(getContext(), R.layout.thong_bao_item, userList);
        listView.setAdapter(adapter);
    }

    public class User{
        public String user_id, fullname, photo;

        public User(String fullname, String photo, String user_id) {
            this.fullname = fullname;
            this.photo = photo;
            this.user_id = user_id;
        }
    }


    public class UserAdapter extends ArrayAdapter<User>{

        LayoutInflater inflater;
        List<User> list;
        int layoutId;

        public UserAdapter(Context context, int resource, List<User> objects) {
            super(context, resource, objects);

            inflater = LayoutInflater.from(context);
            this.layoutId = resource;
            list = objects;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if(v == null)
                v = inflater.inflate(layoutId, null);

            final User user = list.get(position);

            TextView txtInfo = (TextView) v.findViewById(R.id.txtInfo);

            txtInfo.setText(user.fullname);

            CircleImageView circleImageView = (CircleImageView) v.findViewById(R.id.imageAvatar);

            Glide.with(getContext()).using(new FirebaseImageLoader()).load(storageReference.child(user.user_id).child(user.photo)).into(circleImageView);
            v.findViewById(R.id.txtTime).setVisibility(View.GONE);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ProfileActivity.class);
                    intent.putExtra("user_id", user.user_id);
                    getContext().startActivity(intent);
                }
            });

            return v;
        }
    }
}

