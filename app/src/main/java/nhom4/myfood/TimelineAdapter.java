package nhom4.myfood;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by HP on 5/29/2017.
 */

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ItemViewHolder> {
    int layoutId;
    HashMap<String, ArrayList<ItemTimeline>> list;
    Context context;

    public TimelineAdapter(Context context, int layoutId, HashMap<String, ArrayList<ItemTimeline>> list) {
        this.layoutId = layoutId;
        this.list = list;
        this.context = context;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new ItemViewHolder(inflater.inflate(this.layoutId, null));
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {

        int i = 0;

        ArrayList<ItemTimeline> items = new ArrayList<>();

        for(Map.Entry<String, ArrayList<ItemTimeline>> item: list.entrySet())
        {
            if(i == position) {
                items = item.getValue();
                holder.txtDate.setText(item.getKey());
                break;
            }
            i++;
        }

        TimelineItemAdapter adapter = new TimelineItemAdapter(context, R.layout.item_timeline_layout, items);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        holder.recyclerView.setAdapter(adapter);
        holder.recyclerView.setLayoutManager(layoutManager);
        holder.recyclerView.setFocusable(false);
        holder.recyclerView.addItemDecoration(new ItemOffsetDecoration(5));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{

        RecyclerView recyclerView;
        TextView txtDate;

        public ItemViewHolder(View itemView) {
            super(itemView);

            recyclerView = (RecyclerView) itemView.findViewById(R.id.recycleView);
            txtDate = (TextView) itemView.findViewById(R.id.txtDate);
        }
    }
}
