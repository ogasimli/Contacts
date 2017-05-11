package ogasimli.org.contacts.object;

import com.google.gson.annotations.SerializedName;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Orkhan Gasimli on 10.05.2017.
 */

public class PhoneNumber implements Parcelable {

    @SerializedName("mobile")
    private String mobile;

    @SerializedName("home")
    private String home;

    @SerializedName("office")
    private String office;

    public PhoneNumber() {
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getOffice() {
        return office;
    }

    public void setOffice(String office) {
        this.office = office;
    }

    @Override
    public int describeContents() {
        return 0;

    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(this.mobile);
        dest.writeString(this.home);
        dest.writeString(this.office);
    }

    private PhoneNumber(Parcel in) {
        this.mobile = in.readString();
        this.home = in.readString();
        this.office = in.readString();
    }

    public static final Creator<PhoneNumber> CREATOR = new Creator<PhoneNumber>() {
        @Override
        public PhoneNumber createFromParcel(Parcel source) {
            return new PhoneNumber(source);
        }

        @Override
        public PhoneNumber[] newArray(int size) {
            return new PhoneNumber[size];
        }
    };
}
