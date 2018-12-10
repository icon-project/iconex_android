package foundation.icon.iconex.control;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by js on 2018. 3. 14..
 */

public class BottomSheetMenu implements Parcelable {

    private String name;
    private String tag;
    private int resource;

    public BottomSheetMenu(int resource, String name) {
        setResource(resource);
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(resource);
        dest.writeString(tag);
    }

    public static final Parcelable.Creator<BottomSheetMenu> CREATOR
            = new Parcelable.Creator<BottomSheetMenu>() {
        public BottomSheetMenu createFromParcel(Parcel in) {
            return new BottomSheetMenu(in);
        }

        public BottomSheetMenu[] newArray(int size) {
            return new BottomSheetMenu[size];
        }
    };

    private BottomSheetMenu(Parcel in) {
        name = in.readString();
        resource = in.readInt();
        tag = in.readString();
    }
}
