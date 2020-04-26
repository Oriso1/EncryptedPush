package encryptedpush.com.encryptedpush.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.util.concurrent.Executor;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import encryptedpush.com.encryptedpush.R;
import encryptedpush.com.encryptedpush.fcm.MyFirebaseMessagingService;

public class GeneralUtils
{
    private static final int REQUEST_CODE =111;

    /**
     * Setting alarm manager once for specific time
     * @param context
     * @param intent
     * @param delayInMillis
     */
    public static void setAlarm(Context context,Intent intent,long delayInMillis)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(alarmManager!=null)
        {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayInMillis, pendingIntent);
        }
    }

    public static boolean checkIfBiometricSupported(Context context)
    {
        BiometricManager biometricManager = BiometricManager.from(context);
        return biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS;
    }

    /**
     * create popup with biometric popup for fragments
     * @param context
     * @param fragment
     * @param callback
     */
    public static void createFingerprintAuth(final Context context,Fragment fragment,BiometricPrompt.AuthenticationCallback callback)
    {
        Executor executor = ContextCompat.getMainExecutor(context);
        final BiometricPrompt biometricPrompt = new BiometricPrompt(fragment, executor, callback);

        final BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(context.getString(R.string.biometric_message_title))
                .setSubtitle(context.getString(R.string.biometric_message_message))
                .setNegativeButtonText(context.getString(R.string.cancel))
                .build();

        biometricPrompt.authenticate(promptInfo);

    }

    public static String getFCMToken(Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(MyFirebaseMessagingService.FCM_TOKEN, "");
    }

    public static void hideKeyboard(Activity activity){
        if(activity!=null)
        {
            View view = activity.findViewById(android.R.id.content);
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm!=null)
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public static void sendTextToActivity(Activity activity,final TextView textView, final String text, final boolean append)
    {
        if(activity!=null)
        {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(textView!=null) {
                        if (append)
                            textView.append(text);
                        else
                            textView.setText(text);
                    }
                }
            });
        }
    }
}

