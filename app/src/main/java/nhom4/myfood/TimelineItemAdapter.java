package nhom4.myfood;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

/**
 * Created by HP on 5/29/2017.
 */

public class TimelineItemAdapter extends RecyclerView.Adapter<TimelineItemAdapter.ItemViewHolder> {

    int layoutId;
    ArrayList<ItemTimeline> list;
    LayoutInflater inflater;
    Context context;

    public TimelineItemAdapter(Context context, int layoutId, ArrayList<ItemTimeline> list) {
        this.layoutId = layoutId;
        this.list = list;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(inflater.inflate(layoutId, null));
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        final ItemTimeline item = list.get(position);

        holder.txtTitle.setText(item.title);
        holder.txtComment.setText(item.comment_count + "");
        holder.txtLike.setText(item.like_count + "");
        Glide.with(context).using(new FirebaseImageLoader()).load(item.image).into(holder.imageView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BaiVietActivity.class);
                intent.putExtra("user_id", item.user_id);
                intent.putExtra("id", item.id);

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{

        TextView txtTitle, txtLike, txtComment;
        ImageView imageView;

        public ItemViewHolder(View itemView) {
            super(itemView);

            txtTitle = (TextView) itemView.findViewById(R.id.txtTitle);
            txtLike = (TextView) itemView.findViewById(R.id.txtLike);
            txtComment = (TextView) itemView.findViewById(R.id.txtComment);
            imageView = (ImageView) itemView.findViewById(R.id.imageViewCover);
        }
    }
}
