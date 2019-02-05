package nhom4.myfood;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ortiz.touch.TouchImageView;

import java.util.ArrayList;
import java.util.List;

import static nhom4.myfood.R.id.imageView;

public class ImageViewActivity extends AppCompatActivity {

    ArrayList<StorageReference> list = new ArrayList<>();
    ImageSwipeAdapter adapter;
    ViewPager viewPager;
    int pos = 0;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference().child("users");
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference().child("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_swipe_view);

        pos = getIntent().getIntExtra("pos_image", 0);

        final String user_id = getIntent().getStringExtra("user_id");
        final String id = getIntent().getStringExtra("id");

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        adapter = new ImageSwipeAdapter(this, list);
        viewPager.setAdapter(adapter);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot post = dataSnapshot.child(user_id).child("posts").child(id);
                String []images = post.child("images").getValue().toString().split(";");

                for(String image: images)
                    list.add(storageReference.child(user_id).child(id).child(image));

                adapter.notifyDataSetChanged();
                viewPager.setCurrentItem(pos);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class ImageSwipeAdapter extends PagerAdapter{

        private Context context;
        private List<StorageReference> list;

        public ImageSwipeAdapter(Context context, List<StorageReference> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final TouchImageView touchImageView = new TouchImageView(context);
            touchImageView.setAdjustViewBounds(true);

            Glide.with(context).using(new FirebaseImageLoader()).load(list.get(position)).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    touchImageView.setImageBitmap(resource);
                }
            });

            container.addView(touchImageView, 0);
            return touchImageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((TouchImageView) object);
        }
    }
}
