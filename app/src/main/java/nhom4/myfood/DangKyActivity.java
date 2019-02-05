package nhom4.myfood;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.Resource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class DangKyActivity extends AppCompatActivity {


    FirebaseAuth auth = FirebaseAuth.getInstance();

    CircleImageView circleImageView;

    ProgressDialog progressDialog;

    Uri image = null;

    boolean gt = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_ky);

        final EditText editTextDate = (EditText) findViewById(R.id.editTextDate);

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        progressDialog = new ProgressDialog(getApplicationContext(), R.style.MyDialogTheme);
        progressDialog.setTitle("Tải ảnh");

        Calendar cal = Calendar.getInstance();

        final DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.MyDialogTheme,new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                editTextDate.setText(simpleDateFormat.format(calendar.getTime()));
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        final EditText editTextTen = (EditText) findViewById(R.id.editTextTen);
        final EditText editPass = (EditText) findViewById(R.id.editTextMK);
        final EditText editRPass = (EditText) findViewById(R.id.editTextRMK);
        final EditText editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        circleImageView = (CircleImageView) findViewById(R.id.photo);

        Button btnChangeImage = (Button) findViewById(R.id.btnChangeImage);
        btnChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Chọn ảnh đại diện"), 0);
            }
        });

        Button btnRegister = (Button) findViewById(R.id.btn);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString();
                final String hoten = editTextTen.getText().toString();
                String ngaysinh = editTextDate.getText().toString();
                String matkhau = editPass.getText().toString();
                String rmatkhau = editRPass.getText().toString();
                final String date = editTextDate.getText().toString();
                gt = false;

                switch (radioGroup.getCheckedRadioButtonId())
                {
                    case R.id.rNam: gt = true; break;
                    case R.id.rNu: gt = false; break;
                }

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    Toast.makeText(DangKyActivity.this, "Nhập email không hợp lệ", Toast.LENGTH_LONG).show();
                    return;
                }

                if(hoten.isEmpty() || ngaysinh.isEmpty() || matkhau.isEmpty() || rmatkhau.isEmpty() || date.isEmpty())
                {
                    Toast.makeText(DangKyActivity.this, "Nhập thông tin không hợp lệ", Toast.LENGTH_LONG).show();
                    return;
                }

                if(matkhau.compareTo(rmatkhau)!=0)
                {
                    Toast.makeText(DangKyActivity.this, "Mật khẩu nhập lại không trùng nhau", Toast.LENGTH_LONG).show();
                    return;
                }

                auth.createUserWithEmailAndPassword(email, matkhau).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            createAcc(user.getUid(), hoten, date, gt);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Địa chỉ email này đã được đăng ký. Hãy nhập địa chỉ email khác", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    void createAcc(String uid, String hoten, String date, boolean gt)
    {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference().child("users").child(uid);

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference().child("users").child(uid);

        image = Uri.parse("android.resource://nhom4.myfood/drawable/avatar");

        String photo = Utils.getFileName(getContentResolver(), image);

        final HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("fullname", hoten);
        hashMap.put("birthday", date);
        hashMap.put("created", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        hashMap.put("photo", photo);
        hashMap.put("gender", !gt ? 0 : 1);

        UploadTask uploadTask = storageReference.child(photo).putFile(image);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(DangKyActivity.this, "Đăng ký thành công", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DangKyActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                FirebaseAuth.getInstance().getCurrentUser().delete();
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
                    Glide.with(DangKyActivity.this).load(image).into(circleImageView);
                }
            }
        }
    }
}
