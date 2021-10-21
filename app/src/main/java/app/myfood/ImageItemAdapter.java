package app.myfood;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;

import java.util.List;

/**
 * Created by HP on 4/28/2017.
 */

public class ImageItemAdapter extends RecyclerView.Adapter<ImageItemAdapter.Item> {

    List<StorageReference> list;
    Context context;

    public ImageItemAdapter(List<StorageReference> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public Item onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new Item(inflater.inflate(R.layout.image_layout, null));
    }

    @Override
    public void onBindViewHolder(Item holder, final int position) {
        Glide.with(context).using(new FirebaseImageLoader()).load(list.get(position)).into(holder.image);

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ImageViewActivity.class);
                intent.putExtra("pos_image", position);

                String user_id, id;

                id = list.get(0).getParent().getName();

                user_id = list.get(0).getParent().getParent().getName();

                intent.putExtra("user_id", user_id);
                intent.putExtra("id", id);

                context.startActivity(intent);
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
            button.setVisibility(View.GONE);
        }
    }
}
