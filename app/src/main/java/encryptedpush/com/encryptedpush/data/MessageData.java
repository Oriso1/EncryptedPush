package encryptedpush.com.encryptedpush.data;

import android.os.Parcel;
import android.os.Parcelable;

public class MessageData implements Parcelable
{
    protected String title;
    protected String message;
    protected String payload;
    protected String encryptedData;

    public MessageData(String title,String message,String payload,String encryptedData)
    {
        this.title=title;
        this.message = message;
        this.payload = payload;
        this.encryptedData =encryptedData;
    }


    protected MessageData(Parcel in) {
        title = in.readString();
        message = in.readString();
        payload = in.readString();
        encryptedData = in.readString();
    }

    public static final Creator<MessageData> CREATOR = new Creator<MessageData>() {
        @Override
        public MessageData createFromParcel(Parcel in) {
            return new MessageData(in);
        }

        @Override
        public MessageData[] newArray(int size) {
            return new MessageData[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(message);
        dest.writeString(payload);
        dest.writeString(encryptedData);
    }
}
