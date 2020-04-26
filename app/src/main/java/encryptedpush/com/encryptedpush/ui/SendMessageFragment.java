package encryptedpush.com.encryptedpush.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import encryptedpush.com.encryptedpush.receivers.AlarmReceiver;
import encryptedpush.com.encryptedpush.utils.GeneralUtils;
import encryptedpush.com.encryptedpush.MyApp;
import encryptedpush.com.encryptedpush.R;
import encryptedpush.com.encryptedpush.cipher.KeyStoreHelper;

public class SendMessageFragment extends Fragment {

    private static final String TAG = "SendMessageFragment";
    private static final long DELAY_TIME_IN_SEC = 15;
    public static final String IS_BIOMETRIC_CHECKED = "is_biometric_checked";


    private NavController navController;
    private AppCompatButton sendButton;
    private CheckBox biometricCheckbox;
    private TextInputEditText textInput;
    private TextView statusTextview;
    private boolean isSent;
    private String encryptedMsg,payload;
    private SharedPreferences preferences;
    private ExecutorService threadExecutor;
    private ConstraintLayout constraintLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.send_message_fragment, container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        threadExecutor = Executors.newSingleThreadExecutor();

        preferences =PreferenceManager.getDefaultSharedPreferences(getContext());

        navController = Navigation.findNavController(view);
        sendButton = view.findViewById(R.id.send_button);
        biometricCheckbox = view.findViewById(R.id.checkbox_biometric);
        textInput = view.findViewById(R.id.text_input);
        statusTextview = view.findViewById(R.id.status_textview);
        constraintLayout = view.findViewById(R.id.main_layout);
        final boolean isBiometricSupported = GeneralUtils.checkIfBiometricSupported(getContext());

        biometricCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(IS_BIOMETRIC_CHECKED,isChecked);
                editor.apply();
            }
        });

        if (!isBiometricSupported) {
            biometricCheckbox.setEnabled(false);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(IS_BIOMETRIC_CHECKED,false);
            editor.apply();

        }
        else {
            biometricCheckbox.setChecked(true);
        }



        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(!textInput.getText().toString().isEmpty())
                {
                    final String messageToEncrypt = textInput.getText().toString();

                    // since encryption can be an "expensive" work it will be done on different thread and not on main thread
                    threadExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.d(TAG,"start encrypting");
                                encryptedMsg = KeyStoreHelper.encrypt(MyApp.ENC_DEC,messageToEncrypt);
                                Log.d(TAG,"encryption done");
                                GeneralUtils.sendTextToActivity(getActivity(),statusTextview,getString(R.string.encryption_done),false);
                                Log.d(TAG,"start signing");
                                payload = KeyStoreHelper.signData(MyApp.SIGN_VALIDATING,encryptedMsg);
                                Log.d(TAG,"signing done");
                                GeneralUtils.sendTextToActivity(getActivity(),statusTextview,getString(R.string.Signing_done),true);
                                GeneralUtils.sendTextToActivity(getActivity(),statusTextview,getString(R.string.exit_app),true);

                                isSent = true;
                            }
                            catch (Exception e){
                                e.printStackTrace();
                                payload ="";
                                GeneralUtils.sendTextToActivity(getActivity(),statusTextview,"Error",false);
                            }
                        }
                    });
                    GeneralUtils.hideKeyboard(getActivity());
                    textInput.setText("");
                }
                else
                {
                    Snackbar snackbar = Snackbar.make(constraintLayout, R.string.cant_send_empty, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }

            }

        });

    }

    @Override
    public void onStop() {
        super.onStop();

        if(isSent){
            if(isAdded() && getActivity()!= null) {

                //Apparently Alarm manager receivers have problem to get custom parcelable objects so I'm sending it many strings
                // https://stackoverflow.com/a/39478479
                Intent intent = new Intent(getActivity().getApplicationContext(), AlarmReceiver.class);
                intent.putExtra(AlarmReceiver.MESSAGE_TO_SHOW,getString(R.string.notification_text));
                intent.putExtra(AlarmReceiver.TITLE_TO_SHOW,getString(R.string.notification_title));
                intent.putExtra(AlarmReceiver.ENCRYPTED_MESSAGE,encryptedMsg);
                intent.putExtra(AlarmReceiver.PAYLOAD,payload);

                Log.d(TAG, "Setting Alarm for "+DELAY_TIME_IN_SEC+" seconds from now");
                GeneralUtils.setAlarm(getActivity().getApplicationContext(),intent,TimeUnit.SECONDS.toMillis(DELAY_TIME_IN_SEC));
            }
            isSent = false;
        }

    }

}
