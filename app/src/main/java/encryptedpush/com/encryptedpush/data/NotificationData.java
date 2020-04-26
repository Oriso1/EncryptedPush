package encryptedpush.com.encryptedpush.data;

import android.os.Parcel;
import android.os.Parcelable;

public class NotificationData extends MessageData implements Parcelable
{
    private String type;


    public NotificationData(String title,String message,String type,String payload,String encryptedData)
    {
        super(title,message,payload,encryptedData);
        this.type = type;
    }
    public NotificationData(MessageData messageData,String type)
    {
        super(messageData.getTitle(),messageData.getMessage(),messageData.getPayload(),messageData.getEncryptedData());
        this.type = type;
    }

    protected NotificationData(Parcel in) {
        super(in);
        type = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NotificationData> CREATOR = new Creator<NotificationData>() {
        @Override
        public NotificationData createFromParcel(Parcel in) {
            return new NotificationData(in);
        }

        @Override
        public NotificationData[] newArray(int size) {
            return new NotificationData[size];
        }
    };

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
