package nhom4.myfood;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

/**
 * Created by HP on 5/30/2017.
 */

public class ItemSuggestion implements SearchSuggestion {

    String user_id;
    String post_id;
    String content;

    public ItemSuggestion(String content, String post_id, String user_id) {
        this.content = content;
        this.post_id = post_id;
        this.user_id = user_id;
    }

    public ItemSuggestion(Parcel parcel)
    {
        this.user_id = parcel.readString();
        this.post_id = parcel.readString();
        this.content = parcel.readString();
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public String getBody() {
        return this.content;
    }

    public static final Creator<ItemSuggestion> CREATOR = new Creator<ItemSuggestion>(){
        @Override
        public ItemSuggestion createFromParcel(Parcel source) {
            return new ItemSuggestion(source);
        }

        @Override
        public ItemSuggestion[] newArray(int size) {
            return new ItemSuggestion[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
        dest.writeString(post_id);
        dest.writeString(content);
    }
}
