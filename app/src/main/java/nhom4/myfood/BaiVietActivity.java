package nhom4.myfood;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class BaiVietActivity extends AppCompatActivity {

    RecyclerView view;
    CustomAdapter adapter;
    ArrayList<StorageReference> list;
    CircleImageView imageAvatar;
    TextView txtName;
    TextView txtTime;
    TextView txtContent;
    TextView txtTitle;
    TextView txtLike;
    TextView txtComment;
    String user_id, id;
    String []images;
    long cm = 0;
    long like = 0;
    boolean liked = false;

    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference = firebaseStorage.getReference().child("users");

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference().child("users");

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser current_user = auth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bai_viet);
        list = new ArrayList<StorageReference>();

        imageAvatar = (CircleImageView) findViewById(R.id.imageAvatar);
        txtName = (TextView) findViewById(R.id.txtName);
        txtTime = (TextView) findViewById(R.id.txtTime);
        txtContent = (TextView) findViewById(R.id.txtContent);
        txtLike = (TextView) findViewById(R.id.txtLike);
        txtComment = (TextView) findViewById(R.id.txtCNumber);
        txtTitle = (TextView) findViewById(R.id.txtTitle);

        user_id = getIntent().getStringExtra("user_id");
        id = getIntent().getStringExtra("id");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Bài viết");

        view = (RecyclerView) findViewById(R.id.imageRecycleView);

        adapter = new CustomAdapter(this, list);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        //view.setHasFixedSize(true);
        view.setAdapter(adapter);

        view.setLayoutManager(layoutManager);
        //view.setNestedScrollingEnabled(true);
        view.setFocusable(false);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                DataSnapshot user = dataSnapshot.child(user_id);

                String fullname = user.child("fullname").getValue().toString();
                String photo = user.child("photo").getValue().toString();

                if(!user.child("posts").hasChild(id))
                {
                    Toast.makeText(getApplicationContext(), "Bài viết này đã bị xoá", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                String time = user.child("posts").child(id).child("created").getValue().toString();

                if(user.child("posts").child(id).hasChild("modified"))
                {
                    time = user.child("posts").child(id).child("modified").getValue().toString() + " Đã chỉnh sửa";
                }

                txtName.setText(fullname);
                Glide.with(getApplicationContext())
                        .using(new FirebaseImageLoader())
                        .load(storageReference.child(user_id).child(photo)).into(imageAvatar);

                txtName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(BaiVietActivity.this, ProfileActivity.class);
                        intent.putExtra("user_id", user_id);
                        startActivity(intent);
                    }
                });

                txtTime.setText(time);

                txtContent.setText(user.child("posts").child(id).child("content").getValue().toString());

                txtTitle.setText(user.child("posts").child(id).child("title").getValue().toString());

                images = user.child("posts").child(id).child("images").getValue().toString().split(";");

                for(String image : images)
                    list.add(storageReference.child(user_id).child(id).child(image));

                cm = user.child("posts").child(id).child("comments").getChildrenCount();
                like = user.child("posts").child(id).child("like").getChildrenCount();

                DataSnapshot db_like = user.child("posts").child(id).child("like");

                if(db_like.hasChild(current_user.getUid()))
                    liked = true;

                txtLike.setText(like + " lượt thích");

                txtLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        DatabaseReference dr_like = databaseReference.child(user_id).child("posts").child(id).child("like");

                        if(liked)
                        {
                            dr_like.child(current_user.getUid()).removeValue();
                            liked = false;
                            like--;
                        }
                        else
                        {
                            DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                            String date = df.format(new Date());
                            dr_like.child(current_user.getUid()).setValue(date);
                            like++;
                            liked = true;

                            if(!current_user.getUid().equals(user_id))
                                Utils.pushNotification(user_id, id, date, current_user.getUid(), dataSnapshot.child(current_user.getUid()).child("fullname").getValue().toString(), dataSnapshot.child(current_user.getUid()).child("photo").getValue().toString(), null, Notification_Type.LIKE);
                        }

                        ChangeHeart();
                        txtLike.setText(like + " lượt thích");
                    }
                });

                txtComment.setText(cm + " bình luận");

                txtComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CustomDialog dialog = new CustomDialog(BaiVietActivity.this, user_id, id, txtComment);
                        dialog.setContentView(R.layout.dialog_comment);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.show();
                    }
                });

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void ChangeHeart()
    {
        if(liked)
        {
            txtLike.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart, 0, 0, 0);
        }
        else
        {
            txtLike.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_favorite_border_black_24dp, 0, 0, 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(user_id.equals(current_user.getUid())) {
            getMenuInflater().inflate(R.menu.popup_menu, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                Intent intent = new Intent(getApplicationContext(), DangBaiActivity.class);
                intent.putExtra("post_id", id);
                intent.putExtra("isEdit", true);
                startActivity(intent);
                finish();
                return true;
            case R.id.delete:

                databaseReference.child(current_user.getUid()).child("posts").child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            storageReference.child(current_user.getUid()).child(id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Đã xoá bài viết", Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                }
                            });
                        }
                    }
                });

                return true;
        }
        return false;
    }

    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.Item>{

        Context context;
        ArrayList<StorageReference> list;

        public CustomAdapter(Context context, ArrayList<StorageReference> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public Item onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            return new Item(inflater.inflate(R.layout.image_layout, null));
        }

        @Override
        public void onBindViewHolder(Item holder, final int position) {

            Glide.with(getApplicationContext()).using(new FirebaseImageLoader()).load(list.get(position)).into(holder.image);

            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BaiVietActivity.this, ImageViewActivity.class);

                    intent.putExtra("images", images);
                    intent.putExtra("pos_image", position);
                    intent.putExtra("user_id", user_id);
                    intent.putExtra("id", id);

                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }


        public class Item extends RecyclerView.ViewHolder{
            ImageView image;
            public Item(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.imageView);
                Button button = (Button) itemView.findViewById(R.id.button);
                button.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
