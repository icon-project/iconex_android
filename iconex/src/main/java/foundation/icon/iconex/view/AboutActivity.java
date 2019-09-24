package foundation.icon.iconex.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.iconex.R;

public class AboutActivity extends AppCompatActivity {

    public static final String PARAM_ABOUT_ITEM_LIST = "PARAM_ABOUT_ITEM_LIST";

    ArrayList<Parcelable> lstAboutItem = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            lstAboutItem = intent.getParcelableArrayListExtra(PARAM_ABOUT_ITEM_LIST);
        }


        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                int resLayout = viewType == 1 ? R.layout.item_about_head : R.layout.item_about_paragraph;
                View view = LayoutInflater.from(parent.getContext()).inflate(resLayout, parent, false);
                return new AboutItemViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                AboutItem aboutItem = (AboutItem) lstAboutItem.get(position);
                String text = (aboutItem.type == 2 ? "· " : "") + aboutItem.text;
                ((AboutItemViewHolder) holder).text.setText(text);
            }

            @Override
            public int getItemViewType(int position) {
                return ((AboutItem) lstAboutItem.get(position)).type;
            }

            @Override
            public int getItemCount() {
                return lstAboutItem.size();
            }
        });
    }

    static class AboutItemViewHolder extends RecyclerView.ViewHolder {

        public TextView text;

        public AboutItemViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
        }
    }

    public static class AboutItem implements Parcelable {

        public static final int TYPE_HEAD = 1;
        public static final int TYPE_PARAGRAPH = 2;

        public int type;
        public String text;

        public AboutItem() { }

        public AboutItem(int type, String text) {
            this.type = type;
            this.text = text;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.type);
            dest.writeString(this.text);
        }

        protected AboutItem(Parcel in) {
            this.type = in.readInt();
            this.text = in.readString();
        }

        public static final Parcelable.Creator<AboutItem> CREATOR = new Parcelable.Creator<AboutItem>() {
            @Override
            public AboutItem createFromParcel(Parcel source) {
                return new AboutItem(source);
            }

            @Override
            public AboutItem[] newArray(int size) {
                return new AboutItem[size];
            }
        };
    }
}
