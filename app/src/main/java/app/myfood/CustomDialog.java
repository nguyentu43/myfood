package app.myfood;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by HP on 5/1/2017.
 */

public class CustomDialog extends Dialog{

    String user_id, id;
    String fullname, photo;
    CommentAdapter adapter;
    ArrayList<Comment> list = new ArrayList<Comment>();
    ListView listView;
    TextView txtComment;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser user = firebaseAuth.getCurrentUser();

    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference = firebaseStorage.getReference().child("users");

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference().child("users");

    public CustomDialog(Context context, String user_id, String id, TextView cm) {
        super(context);
        this.user_id = user_id;
        this.id = id;
        this.txtComment = cm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GetAllComment();

        final EditText editText = (EditText) findViewById(R.id.editTextContent);

        adapter = new CommentAdapter(getContext(), R.layout.comment_layout, list);
        listView = (ListView) findViewById(R.id.listViewComment);
        listView.setAdapter(adapter);

        Button btnSend = (Button) findViewById(R.id.btnSend);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText.getText().toString() == "")
                {
                    Toast.makeText(getContext(), "Bạn chưa nhập nội dung", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference comments = databaseReference.child(user_id).child("posts").child(id).child("comments");

                final String cm_id = comments.push().getKey();

                Date date = new Date();

                DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                final Comment cm = new Comment(editText.getText().toString(), df.format(date), fullname, user.getUid(), id, photo);

                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put(cm_id, cm.toMap());

                comments.updateChildren(hashMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(user_id.compareTo(user.getUid())!=0)
                            Utils.pushNotification(user_id, id, cm.date, user.getUid(), fullname, photo, null, Notification_Type.COMMENT);
                    }
                });
            }
        });
    }

    void GetAllComment()
    {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                DataSnapshot post = dataSnapshot.child(user_id).child("posts").child(id);

                if(!list.isEmpty())
                    list.clear();
                fullname = dataSnapshot.child(user.getUid()).child("fullname").getValue().toString();
                photo = dataSnapshot.child(user.getUid()).child("photo").getValue().toString();

                for (DataSnapshot cm : post.child("comments").getChildren()) {
                    String id = cm.getKey();
                    String user_id = cm.child("user_id").getValue().toString();
                    String fullname = dataSnapshot.child(user_id).child("fullname").getValue().toString();
                    String photo = dataSnapshot.child(user_id).child("photo").getValue().toString();
                    String date = cm.child("date").getValue().toString();
                    String content = cm.child("content").getValue().toString();
                    list.add(new Comment(content, date, fullname, user_id, id, photo));
                }

                txtComment.setText(list.size() + " bình luận");

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    void showPopUpMenu(View v, final int pos)
    {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.inflate(R.menu.popup_menu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.edit: edit_cm(pos); return true;
                    case R.id.delete: delete_cm(pos); return true;
                    default: return false;
                }
            }
        });

        popupMenu.show();
    }

    void delete_cm(int pos)
    {
        if(list.isEmpty()) return;

        DatabaseReference db_cm = databaseReference.child(user_id).child("posts").child(id).child("comments").child(list.get(pos).getId());
        db_cm.removeValue();
    }

    void edit_cm(int pos)
    {
        final Comment cm = list.get(pos);

        final EditText editText = new EditText(getContext());
        editText.setText(cm.getContent());

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setView(editText);

        builder.setTitle("Chỉnh sửa nội dung bình luận");

        builder.setPositiveButton("Thay đổi", new OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {

                if(editText.getText().toString().isEmpty())
                {
                    Toast.makeText(getContext(), "Bạn chưa nhập nội dung", Toast.LENGTH_SHORT).show();
                    return;
                }

                databaseReference.child(user_id).child("posts").child(id).child("comments").child(cm.getId()).child("content").setValue(editText.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Nội dung đã được thay đổi", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            }
        });

        builder.setNegativeButton("Huỷ bỏ", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.create().show();
    }

    public class Comment{
        String id;
        String user_id;
        String content;
        String date;
        String fullname;
        String photo;

        public Comment(String content, String date, String fullname, String user_id, String id, String photo) {
            this.content = content;
            this.date = date;
            this.fullname = fullname;
            this.user_id = user_id;
            this.id = id;
            this.photo = photo;
        }

        public String getContent() {
            return content;
        }

        public String getDate() {
            return date;
        }

        public String getFullname() {
            return fullname;
        }

        public String getUser_id() {
            return user_id;
        }

        public String getId() {
            return id;
        }

        public String getPhoto() {
            return photo;
        }

        public HashMap<String, Object> toMap()
        {
            HashMap<String, Object> map = new HashMap<>();
            map.put("user_id", user_id);
            map.put("date", date);
            map.put("content", content);
            return map;
        }
    }

    public class CommentAdapter extends ArrayAdapter<Comment>{
        LayoutInflater inflater;
        int layoutId;
        ArrayList<Comment> list;

        public CommentAdapter(Context context, int layoutId, ArrayList<Comment> list) {
            super(context, layoutId, list);
            this.layoutId = layoutId;
            this.list = list;
            inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if(v == null) v = inflater.inflate(layoutId, null);

            final Comment cm = list.get(position);

            TextView txtName = (TextView) v.findViewById(R.id.txtName);
            TextView txtTime = (TextView) v.findViewById(R.id.txtTime);
            TextView txtContent = (TextView) v.findViewById(R.id.txtContent);
            CircleImageView image = (CircleImageView) v.findViewById(R.id.imageAvatar);

            txtName.setText(cm.getFullname());
            txtTime.setText(cm.getDate());
            txtContent.setText(cm.getContent());

            StorageReference photo = storageReference.child(cm.getUser_id()).child(cm.photo);

            Glide.with(getContext()).using(new FirebaseImageLoader()).load(photo).into(image);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(cm.getUser_id().compareTo(user.getUid()) == 0)
                    showPopUpMenu(v, position);
                }
            });

            return v;
        }
    }
}
