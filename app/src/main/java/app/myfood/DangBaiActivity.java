package app.myfood;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class DangBaiActivity extends AppCompatActivity {

    ArrayList<Uri> list;
    ArrayList<Uri> remove = new ArrayList<>();
    ArrayList<Uri> insert = new ArrayList<>();
    CustomAdapter adapter;
    EditText txtTitle, txtContent;

    private static final int FROM_CAMERA = 0;

    private static final int FROM_LIBRARY = 1;

    ProgressDialog progressDialog;

    boolean isEdit = false;

    String post_id = "";
    String created="";
    String images="";

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_bai);
        list = new ArrayList<>();

        progressDialog = new ProgressDialog(DangBaiActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Đang tải ảnh lên...");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Đăng bài");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        txtTitle = (EditText) findViewById(R.id.editTitle);
        txtContent = (EditText) findViewById(R.id.editContent);

        Button btn = (Button) findViewById(R.id.btnThem);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String [] items={"Chụp ảnh", "Thư viện"};

                AlertDialog.Builder builder = new AlertDialog.Builder(DangBaiActivity.this);
                builder.setTitle("Chọn ảnh");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(items[which].equals(items[0]))
                        {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, FROM_CAMERA);
                        }
                        else if(items[which].equals(items[1]))
                        {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            intent.setType("image/*, video/*");
                            startActivityForResult(Intent.createChooser(intent, "Select Image"), FROM_LIBRARY);
                        }
                    }
                });

                builder.create().show();
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.imageListRView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        adapter = new CustomAdapter(this, R.layout.image_layout, list);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new ItemOffsetDecoration(5));

        isEdit = getIntent().getBooleanExtra("isEdit", false);

        if(isEdit)
        {
            post_id = getIntent().getStringExtra("post_id");
            databaseReference.child(user.getUid()).child("posts").child(post_id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    txtTitle.setText(dataSnapshot.child("title").getValue().toString());
                    txtContent.setText(dataSnapshot.child("content").getValue().toString());

                    created = dataSnapshot.child("created").getValue().toString();

                    String []images = dataSnapshot.child("images").getValue().toString().split(";");

                    for(String image: images)
                    {
                        if(image.isEmpty()) continue;
                        storageReference.child(user.getUid()).child(post_id).child(image).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if(uri != null) {
                                    list.add(uri);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case FROM_CAMERA:
                if(resultCode == RESULT_OK)
                {
                    if(data.getData() != null)
                        adapter.addItem(data.getData());
                }
                break;
            case FROM_LIBRARY:
                if(resultCode == RESULT_OK)
                {
                    if(data.getData() != null)
                        adapter.addItem(data.getData());
                    else
                    {
                        ClipData clipData = data.getClipData();
                        int size = clipData.getItemCount();
                        for (int i = 0; i<size; ++i)
                        {
                            adapter.addItem(clipData.getItemAt(i).getUri());
                        }
                    }
                }
                break;
        }
    }

    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.Item>{

        ArrayList<Uri> listImageUri;
        Context context;
        int layoutId;

        public CustomAdapter(Context context, int layoutId, ArrayList<Uri> listImageUri) {
            this.context = context;
            this.layoutId = layoutId;
            this.listImageUri = listImageUri;
        }

        @Override
        public Item onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(this.context);
            return new Item(layoutInflater.inflate(this.layoutId, null));
        }

        @Override
        public void onBindViewHolder(Item holder, final int position) {
            Uri uri = listImageUri.get(position);

            Glide.with(context).load(uri).into(holder.image);
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeItem(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return listImageUri.size();
        }

        public void addItem(Uri uri)
        {
            listImageUri.add(uri);

            if(isEdit)
                insert.add(uri);

            notifyDataSetChanged();
        }

        public void removeItem(int pos)
        {
            if(isEdit) {
                if (insert.contains(listImageUri.get(pos)))
                    remove.add(listImageUri.get(pos));
            }
            listImageUri.remove(pos);

            notifyDataSetChanged();
        }

        public class Item extends RecyclerView.ViewHolder{

            private ImageView image;
            private Button button;

            public Item(View itemView) {
                super(itemView);
                this.image = (ImageView) itemView.findViewById(R.id.imageView);
                this.button = (Button) itemView.findViewById(R.id.button);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dang_bai, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId())
        {
            case R.id.action_post:
                if(!isEdit)
                    Save();
                else
                    Edit();
                return true;
        }

        return super.onOptionsItemSelected(item);
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

    void Save()
    {
        String title = txtTitle.getText().toString();
        String content = txtContent.getText().toString();

        if(title.isEmpty() || content.isEmpty())
        {
            Toast.makeText(getApplicationContext(), "Bạn chưa nhập đủ thông tin", Toast.LENGTH_LONG).show();
            return;
        }

        if(list.isEmpty())
        {
            Toast.makeText(getApplicationContext(), "Bạn chưa thêm hình ảnh", Toast.LENGTH_LONG).show();
            return;
        }

        String images = "";

        final String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());

        final HashMap<String, Object> bv = new HashMap<>();
        bv.put("title", title);
        bv.put("content", content);
        bv.put("created", date);
        bv.put("images", images);

        DatabaseReference db_users = databaseReference.child(user.getUid());
        db_users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String fullname = dataSnapshot.child("fullname").getValue().toString();
                final String photo = dataSnapshot.child("photo").getValue().toString();

                final ArrayList<String> list_followed_by = new ArrayList<String>();

                for(DataSnapshot item: dataSnapshot.child("followed_by").getChildren())
                {
                    list_followed_by.add(item.getKey().toString());
                }

                final DatabaseReference db_baiviet = databaseReference.child(user.getUid()).child("posts");

                final String key = db_baiviet.push().getKey().toString();

                StorageReference sr = storageReference.child(user.getUid()).child(key);

                progressDialog.setMax(list.size());
                progressDialog.show();

                for(Uri uri: list)
                {
                    String filename = Utils.getFileName(getContentResolver(), uri);
                    bv.put("images", bv.get("images").toString() + filename + ";");
                    final UploadTask uploadTask = sr.child(filename).putFile(uri);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.incrementProgressBy(1);

                            if(progressDialog.getProgress() == list.size())
                            {
                                progressDialog.dismiss();

                                db_baiviet.child(key).setValue(bv).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getApplicationContext(), "Đã đăng bài thành công", Toast.LENGTH_SHORT).show();
                                        Utils.pushNotification(user.getUid(), key, date, user.getUid(), fullname, photo, list_followed_by, Notification_Type.NEW_POST);
                                    }
                                });
                                onBackPressed();
                            }
                        }
                    });

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Đã có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void Edit()
    {
        final String title = txtTitle.getText().toString();
        final String content = txtContent.getText().toString();

        if(title.isEmpty() || content.isEmpty())
        {
            Toast.makeText(getApplicationContext(), "Bạn chưa nhập đủ thông tin", Toast.LENGTH_LONG).show();
            return;
        }

        if(list.isEmpty())
        {
            Toast.makeText(getApplicationContext(), "Bạn chưa thêm hình ảnh", Toast.LENGTH_LONG).show();
            return;
        }

        final String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());

        DatabaseReference db_users = databaseReference.child(user.getUid());
        db_users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final ArrayList<String> list_followed_by = new ArrayList<String>();

                for(DataSnapshot item: dataSnapshot.child("followed_by").getChildren())
                {
                    list_followed_by.add(item.getKey().toString());
                }

                final DatabaseReference db_baiviet = databaseReference.child(user.getUid()).child("posts");

                final StorageReference sr = storageReference.child(user.getUid()).child(post_id);

                progressDialog.setMax(insert.size() + remove.size());
                progressDialog.setTitle("Đang xử lí...");

                if(!(insert.size() == 0 && remove.size() == 0))
                    progressDialog.show();

                for(Uri uri: list)
                {
                    String filename = Utils.getFileName(getContentResolver(), uri);

                    images +=filename + ";";
                }


                for(Uri uri: insert)
                {
                    String filename = Utils.getFileName(getContentResolver(), uri);

                    sr.child(filename).putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.incrementProgressBy(1);
                            if(progressDialog.getProgress() == progressDialog.getMax())
                                progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Đã có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                for (Uri item : remove) {
                    String filename = Utils.getFileName(getContentResolver(), item);

                    sr.child(filename).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.incrementProgressBy(1);
                            if(progressDialog.getProgress() == progressDialog.getMax())
                                progressDialog.dismiss();
                        }
                    });
                }

                db_baiviet.child(post_id).child("title").setValue(title).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        db_baiviet.child(post_id).child("content").setValue(content).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                db_baiviet.child(post_id).child("modified").setValue(date).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        db_baiviet.child(post_id).child("images").setValue(images).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getApplicationContext(), "Đã cập nhật nội dung", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });

                            }
                        });

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
