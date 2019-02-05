package nhom4.myfood;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class BangTinFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    FloatingSearchView floatingSearchView;

    ArrayList<ItemSuggestion> suggestionList = new ArrayList<>();

    public BangTinFragment() {
        // Required empty public constructor
    }

    RecyclerView recyclerView;
    ArrayList<BaiViet> list;
    ItemAdapter adapter;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference db_users = db.getReference().child("users");

    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference = firebaseStorage.getReference().child("users");

    String current_user_fullname = "";
    String photo = "";
    ArrayList<String> follow = new ArrayList<>();
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bang_tin, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    void GetAllPost()
    {
        if(!list.isEmpty())
            list.clear();

        GetNewPostFollow();
        GetMyPost();
    }

    void GetMyPost()
    {
        db_users.child(user.getUid()).child("posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot item: dataSnapshot.getChildren())
                {
                    GetById(user.getUid(), item.getKey().toString(), current_user_fullname, storageReference.child(user.getUid()).child(photo));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void GetNewPostFollow()
    {
        db_users.child(user.getUid()).child("notifications").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot item: dataSnapshot.getChildren()) {

                    Notification_Type type = Utils.getType(Integer.parseInt(item.child("type").getValue().toString()));

                    if (type == Notification_Type.NEW_POST) {
                        String user_id = item.child("user_id").getValue().toString();
                        String fullname = item.child("fullname").getValue().toString();
                        String post_id = item.child("post_id").getValue().toString();
                        String image = item.child("photo").getValue().toString();
                        StorageReference photo = storageReference.child(user_id).child(image);
                        GetById(user_id, post_id, fullname, photo);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void GetById(final String user_id, final String id, final String fullname, final StorageReference photo)
    {

        DatabaseReference db_post = db_users.child(user_id).child("posts").child(id);
        final StorageReference sr_post = storageReference.child(user_id).child(id);

        db_post.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<StorageReference> list_photo = new ArrayList<StorageReference>();

                String [] list_images = dataSnapshot.child("images").getValue().toString().split(";");

                for (int i = 0; i<list_images.length; ++i)
                    list_photo.add(sr_post.child(list_images[i]));

                String content = dataSnapshot.child("title").getValue().toString();
                String date = dataSnapshot.child("created").getValue().toString();
                String modified = "";

                if(dataSnapshot.hasChild("modified"))
                    modified = dataSnapshot.child("modified").getValue().toString();

                String title = dataSnapshot.child("title").getValue().toString();

                long n = dataSnapshot.child("like").getChildrenCount();
                long cm = dataSnapshot.child("comments").getChildrenCount();

                BaiViet bv = new BaiViet(photo, list_photo, fullname, title, content, user_id, id, date, modified, n, cm);

                if(dataSnapshot.child("like").hasChild(user.getUid()))
                    bv.setLiked(true);

                list.add(bv);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void RefreshList()
    {
        GetAllPost();
        swipeRefreshLayout.setRefreshing(false);
    }

    void SearchName(final String query)
    {
        db_users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                floatingSearchView.showProgress();
                for(DataSnapshot item: dataSnapshot.getChildren())
                {
                    String fullname = item.child("fullname").getValue().toString();

                    if(fullname.toLowerCase().indexOf(query.toLowerCase()) > -1)
                    {
                        suggestionList.add(new ItemSuggestion(fullname, "", item.getKey().toString()));
                    }
                }

                floatingSearchView.swapSuggestions(suggestionList);
                floatingSearchView.hideProgress();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void SearchPost(final String query)
    {
        if(follow.isEmpty()) return;

        for(final String user_id: follow)
        {
            db_users.child(user_id).child("posts").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    floatingSearchView.showProgress();
                    for(DataSnapshot item: dataSnapshot.getChildren())
                    {
                        if(item.child("title").getValue().toString().toLowerCase().indexOf(query.toLowerCase()) > -1)
                        {
                            suggestionList.add(new ItemSuggestion(item.child("title").getValue().toString(), item.getKey().toString(), user_id));
                        }
                    }

                    floatingSearchView.swapSuggestions(suggestionList);
                    floatingSearchView.hideProgress();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        db_users.child(user.getUid()).child("posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                floatingSearchView.showProgress();
                for(DataSnapshot item: dataSnapshot.getChildren())
                {
                    if(item.child("title").getValue().toString().toLowerCase().indexOf(query.toLowerCase()) > -1)
                    {
                        suggestionList.add(new ItemSuggestion(item.child("title").getValue().toString(), item.getKey().toString(), user.getUid()));
                    }
                }

                floatingSearchView.swapSuggestions(suggestionList);
                floatingSearchView.hideProgress();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView = (RecyclerView) getView().findViewById(R.id.bangTinRecycleView);

        floatingSearchView = (FloatingSearchView) getView().findViewById(R.id.fSearchView);

        list = new ArrayList<BaiViet>();

        adapter = new ItemAdapter(list, getContext());

        recyclerView.setAdapter(adapter);

        final LinearLayoutManager layout = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        //StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(layout);

        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RefreshList();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) getView().findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), DangBaiActivity.class);
                startActivity(intent);
            }
        });

        floatingSearchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, final ImageView leftIcon, TextView textView, SearchSuggestion item, int itemPosition) {
                final ItemSuggestion itemSuggestion = (ItemSuggestion) item;

                if(itemSuggestion.getPost_id().isEmpty())
                {
                    //leftIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_supervisor_account_black_24dp));
                    db_users.child(itemSuggestion.user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String photo = dataSnapshot.child("photo").getValue().toString();
                            Glide.with(getContext()).using(new FirebaseImageLoader()).load(storageReference.child(itemSuggestion.user_id).child(photo)).into(leftIcon);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else
                {
                    leftIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_comment_black_24dp));
                }


                textView.setText(itemSuggestion.getBody());
            }
        });

        floatingSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                suggestionList.clear();
                floatingSearchView.swapSuggestions(suggestionList);
                if(!newQuery.isEmpty()) {
                    SearchName(newQuery);
                    SearchPost(newQuery);
                }
            }
        });

        floatingSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                ItemSuggestion itemSuggestion = (ItemSuggestion) searchSuggestion;

                Intent intent;
                if(itemSuggestion.getPost_id().isEmpty())
                {
                    intent = new Intent(getContext(), ProfileActivity.class);
                }
                else
                {
                    intent = new Intent(getContext(), BaiVietActivity.class);
                    intent.putExtra("id", itemSuggestion.getPost_id());
                }
                intent.putExtra("user_id", itemSuggestion.getUser_id());
                startActivity(intent);
            }

            @Override
            public void onSearchAction(String currentQuery) {

            }
        });

        if(user == null)
            return;
    }

    class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemView>{

        List<BaiViet> list;
        Context context;
        LayoutInflater layoutInflater;

        public ItemAdapter(List<BaiViet> list, Context context) {
            this.list = list;
            this.context = context;
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public ItemView onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = layoutInflater.inflate(R.layout.item_bang_tin_layout, parent, false);
            return new ItemView(view);
        }

        @Override
        public void onBindViewHolder(final ItemView holder, final int position) {
            final BaiViet bv = list.get(position);
            holder.txtContent.setText(bv.getContent());
            holder.txtName.setText(bv.getPost_name());

            if(bv.getModified().isEmpty())
                holder.txtTime.setText(bv.getPostTime());
            else
                holder.txtTime.setText(bv.getModified() + " Đã chỉnh sửa");

            holder.txtName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ProfileActivity.class);
                    intent.putExtra("user_id", bv.getUser_id());
                    startActivity(intent);
                }
            });

            Glide.with(getContext()).using(new FirebaseImageLoader()).load(bv.getImageAvatar()).into(holder.imageAvatar);

            List<StorageReference> l = bv.getImage();

            final ImageItemAdapter adapter = new ImageItemAdapter(l, getContext());
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
            holder.imageListView.setHasFixedSize(true);
            holder.imageListView.setLayoutManager(layoutManager);
            holder.imageListView.setAdapter(adapter);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), BaiVietActivity.class);
                    intent.putExtra("user_id", bv.getUser_id());
                    intent.putExtra("id", bv.getId());
                    startActivity(intent);
                }
            });

            holder.txtLike.setText(bv.getLike_count() + " lượt thích");

            holder.txtLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(bv.isLiked())
                    {
                        db_users.child(bv.getUser_id()).child("posts").child(bv.getId()).child("like").child(user.getUid()).removeValue();
                        bv.setLiked(false);
                        bv.setLike_count(bv.getLike_count() - 1);
                    }
                    else
                    {
                        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        String date = df.format(new Date());
                        db_users.child(bv.getUser_id()).child("posts").child(bv.getId()).child("like").child(user.getUid()).setValue(date);
                        bv.setLike_count(bv.getLike_count() + 1);
                        bv.setLiked(true);

                        if(bv.getUser_id().compareTo(user.getUid()) != 0)
                            Utils.pushNotification(bv.getUser_id(), bv.getId(), date, user.getUid(), current_user_fullname, photo, null,Notification_Type.LIKE);
                    }
                    ChangeHeart(holder.txtLike, bv);
                    holder.txtLike.setText(bv.getLike_count() + " lượt thích");
                }
            });

            holder.txtComment.setText(bv.getCm_count() + " bình luận");

            ChangeHeart(holder.txtLike, bv);

            holder.txtComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomDialog dialog = new CustomDialog(getContext(), list.get(position).getUser_id(), list.get(position).getId(), holder.txtComment);
                    dialog.setContentView(R.layout.dialog_comment);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();
                }
            });

            if(bv.getUser_id() == user.getUid()) {
                holder.txtMenu.setVisibility(View.VISIBLE);
                holder.txtMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popupMenu = new PopupMenu(getContext(), holder.txtMenu);
                        popupMenu.inflate(R.menu.popup_menu);
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {

                                switch (item.getItemId()) {
                                    case R.id.edit:

                                        Intent intent = new Intent(getContext(), DangBaiActivity.class);
                                        intent.putExtra("isEdit", true);
                                        intent.putExtra("post_id", list.get(position).getId());
                                        startActivity(intent);

                                        return true;
                                    case R.id.delete:

                                        db_users.child(user.getUid()).child("posts").child(bv.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                storageReference.child(user.getUid()).child(bv.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(getContext(), "Bài viết đã được xoá", Toast.LENGTH_LONG).show();
                                                        GetAllPost();
                                                    }
                                                });
                                            }
                                        });

                                        return true;
                                }

                                return false;
                            }
                        });

                        popupMenu.show();
                    }
                });
            }
        }

        void ChangeHeart(TextView textView, BaiViet bv)
        {
            if(bv.isLiked())
            {
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart, 0, 0, 0);
            }
            else
            {
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_favorite_border_black_24dp, 0, 0, 0);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ItemView extends RecyclerView.ViewHolder{

            CircleImageView imageAvatar;
            TextView txtContent, txtName, txtTime, txtComment, txtLike, txtMenu;
            RecyclerView imageListView;

            public ItemView(View itemView) {
                super(itemView);
                imageAvatar = (CircleImageView) itemView.findViewById(R.id.imageAvatar);
                txtContent = (TextView) itemView.findViewById(R.id.txtContent);
                txtName = (TextView) itemView.findViewById(R.id.txtName);
                imageListView = (RecyclerView) itemView.findViewById(R.id.imageListView);
                txtTime = (TextView) itemView.findViewById(R.id.txtTime);
                txtComment = (TextView) itemView.findViewById(R.id.txtCNumber);
                txtLike = (TextView) itemView.findViewById(R.id.txtLike);
                txtMenu = (TextView) itemView.findViewById(R.id.txtMenu);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        db_users.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                current_user_fullname = dataSnapshot.child("fullname").getValue().toString();
                photo = dataSnapshot.child("photo").getValue().toString();

                if(!follow.isEmpty())
                    follow.clear();

                for(DataSnapshot item: dataSnapshot.child("follow").getChildren())
                {
                    follow.add(item.getKey().toString());
                }

                GetAllPost();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
