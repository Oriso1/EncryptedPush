package encryptedpush.com.encryptedpush.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;
import encryptedpush.com.encryptedpush.R;
import encryptedpush.com.encryptedpush.data.NotificationData;

public class MainActivity extends FragmentActivity {

    public static final String FROM_NOTIFICATION = "from_notification";
    public static final String NOTIFICATION_DATA = "notification_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chooseFragment();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        chooseFragment();
    }

    private void chooseFragment()
    {
        boolean fromNotification = false;

        if(getIntent() !=null) {
            fromNotification = getIntent().getBooleanExtra(FROM_NOTIFICATION, false);
        }

        if(fromNotification)
        {
            NotificationData data  = getIntent().getParcelableExtra(NOTIFICATION_DATA);

            if(data != null)
            {
                SendMessageFragmentDirections.ActionSendMessageFragmentToDecryptedMessageFragment action = SendMessageFragmentDirections.actionSendMessageFragmentToDecryptedMessageFragment(data);
                Navigation.findNavController(this,R.id.nav_host_fragment).navigate(action);
            }
        }
    }

}
