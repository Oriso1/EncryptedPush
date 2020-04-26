package encryptedpush.com.encryptedpush.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.Fragment;
import encryptedpush.com.encryptedpush.utils.GeneralUtils;
import encryptedpush.com.encryptedpush.MyApp;
import encryptedpush.com.encryptedpush.R;
import encryptedpush.com.encryptedpush.cipher.KeyStoreHelper;
import encryptedpush.com.encryptedpush.data.NotificationData;

public class DecryptedMessageFragment extends Fragment {

    private static final String TAG = "DecryptedMessageFragment";

    private NotificationData notificationData;
    private TextView tvMessageText;
    private Button btnBiometricLogin;
    private ExecutorService threadExecutor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.decrypted_message_fragment, container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getArguments()!=null)
        {
            DecryptedMessageFragmentArgs args = DecryptedMessageFragmentArgs.fromBundle(getArguments());
            notificationData = args.getNotificationExtras();

            threadExecutor = Executors.newSingleThreadExecutor();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean isBiometricChecked = preferences.getBoolean(SendMessageFragment.IS_BIOMETRIC_CHECKED, false);
            btnBiometricLogin = view.findViewById(R.id.authenticate_button);
            tvMessageText = view.findViewById(R.id.message_text);
            if(isBiometricChecked)
            {
                GeneralUtils.createFingerprintAuth(getContext(), DecryptedMessageFragment.this, biometricCallback);
                btnBiometricLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GeneralUtils.createFingerprintAuth(getContext(), DecryptedMessageFragment.this, biometricCallback);
                    }
                });
            }

            else
            {
                handleReceivedMessage();
            }

        }
    }

    private void handleReceivedMessage()
    {
        // same as encryption, better to do "expensive" work on dedicated thread
        threadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG,"verifying data");
                    boolean isVerified = KeyStoreHelper.verifyData(MyApp.SIGN_VALIDATING,notificationData.getEncryptedData(),notificationData.getPayload());
                    Log.d(TAG,"verificaton done. isVerified = "+isVerified);
                    if(isVerified) {

                        if (getActivity() != null)
                        {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btnBiometricLogin.setVisibility(View.GONE);
                                    tvMessageText.setText(R.string.signing_is_verified);
                                    tvMessageText.setVisibility(View.VISIBLE);
                                }
                            });
                        }

                        Log.d(TAG,"start decrypting");
                        final String decrypted = KeyStoreHelper.decrypt(MyApp.ENC_DEC, notificationData.getEncryptedData());
                        Log.d(TAG, "Decrypted message = " + decrypted);
                        GeneralUtils.sendTextToActivity(getActivity(),tvMessageText,getString(R.string.your_message_text_is,decrypted),true);
                    }
                    else {
                        Log.d(TAG, "verification is wrong");
                        GeneralUtils.sendTextToActivity(getActivity(), tvMessageText, getString(R.string.verification_error), false);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }


    private BiometricPrompt.AuthenticationCallback biometricCallback = new BiometricPrompt.AuthenticationCallback(){
        @Override
        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            Log.d(TAG,"Authentication error: " + errString);
        }
        @Override
        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            Log.d(TAG,"Authentication succeeded!");
            handleReceivedMessage();
        }
        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            Log.d(TAG,"Authentication failed!");

        }
    };

}
