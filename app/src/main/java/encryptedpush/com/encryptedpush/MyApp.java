package encryptedpush.com.encryptedpush;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;

import encryptedpush.com.encryptedpush.cipher.KeyStoreHelper;

public class MyApp extends Application {

    private static final String TAG = "MyApp";

    public static final String ENC_DEC = "enc_dec";
    public static final String SIGN_VALIDATING = "sign_verify";
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

        try {
            KeyStoreHelper.createKeys(ENC_DEC);
            KeyStoreHelper.createKeysForSigning(SIGN_VALIDATING);
        } catch (Exception e) {

            Log.e(TAG,"Error creating keys");
            e.printStackTrace();
        }

    }
}
