package nhom4.myfood;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {

    EditText editTextTen;
    EditText ngaysinh;
    EditText matkhau, matkhaumoi;
    RadioGroup gioitinh;
    RadioButton rNam, rNu;
    CircleImageView circleImageView;
    Uri image = null;
    Button btnUpdate;
    Button btnResetPassword;
    HashMap<String, Object> profile = new HashMap<>();

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        editTextTen = (EditText) findViewById(R.id.editTextTen);
        ngaysinh = (EditText) findViewById(R.id.editTextDate);
        gioitinh = (RadioGroup) findViewById(R.id.radioGroup);
        matkhau = (EditText) findViewById(R.id.editTextMK);
        matkhaumoi = (EditText) findViewById(R.id.editTextMKM);
        rNam = (RadioButton) findViewById(R.id.rNam);
        rNu = (RadioButton) findViewById(R.id.rNu);
        circleImageView = (CircleImageView) findViewById(R.id.photo);
        btnUpdate = (Button) findViewById(R.id.btn);

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        Calendar cal = Calendar.getInstance();

        final DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.MyDialogTheme,new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                ngaysinh.setText(simpleDateFormat.format(calendar.getTime()));
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        ngaysinh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        Button btnChangeImage = (Button) findViewById(R.id.btnChangeImage);
        btnChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Chọn ảnh đại diện"), 0);
            }
        });

        FetchInfo();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Change();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 0)
        {
            if(resultCode == RESULT_OK)
            {
                if(data.getData() != null)
                {
                    image = data.getData();
                    Glide.with(getApplicationContext()).load(image).into(circleImageView);
                }
            }
        }
    }

    void FetchInfo()
    {
        databaseReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                editTextTen.setText(dataSnapshot.child("fullname").getValue().toString());

                if(dataSnapshot.hasChild("gender"))
                {
                    if(dataSnapshot.child("gender").getValue().toString().compareTo("0") == 0)
                    {
                        rNu.setChecked(true);
                    }
                    else
                    {
                        rNam.setChecked(true);
                    }
                }

                if(dataSnapshot.hasChild("birthday"))
                {
                    ngaysinh.setText(dataSnapshot.child("birthday").getValue().toString());
                }

                StorageReference sr_photo = storageReference.child(user.getUid()).child(dataSnapshot.child("photo").getValue().toString());

                Glide.with(getApplicationContext()).using(new FirebaseImageLoader()).load(sr_photo).into(circleImageView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void Change()
    {
        if(matkhau.getText().toString().isEmpty())
        {
            Toast.makeText(getApplicationContext(), "Bạn chưa nhập mật khẩu hiện tại", Toast.LENGTH_LONG).show();
            return;
        }

        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), matkhau.getText().toString());
        user.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    if(!matkhaumoi.getText().toString().isEmpty())
                    {
                        user.updatePassword(matkhaumoi.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    Toast.makeText(getApplicationContext(), "Mật khẩu mới đã được cập nhật", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                        if(!editTextTen.getText().toString().isEmpty())
                        {
                            databaseReference.child(user.getUid()).child("fullname").setValue(editTextTen.getText().toString());
                        }

                        if(gioitinh.getCheckedRadioButtonId() == R.id.rNam)
                            databaseReference.child(user.getUid()).child("gender").setValue(1);
                        else
                            databaseReference.child(user.getUid()).child("gender").setValue(0);

                        databaseReference.child(user.getUid()).child("birthday").setValue(ngaysinh.getText().toString());

                        if(image != null)
                        {
                            storageReference.child(user.getUid()).child("photo").child(Utils.getFileName(getContentResolver(), image)).putFile(image).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(getApplicationContext(), "Đã cập nhật ảnh", Toast.LENGTH_LONG).show();
                                        databaseReference.child(user.getUid()).child("photo").setValue(Utils.getFileName(getContentResolver(), image));
                                    }
                                }
                            });
                        }

                        Toast.makeText(getApplicationContext(), "Đã cập nhật thông tin", Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Mật khẩu hiện tại không chính xác", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
