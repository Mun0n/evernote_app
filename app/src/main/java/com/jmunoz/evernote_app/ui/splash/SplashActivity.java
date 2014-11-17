package com.jmunoz.evernote_app.ui.splash;

import com.evernote.client.android.EvernoteSession;
import com.jmunoz.evernote_app.R;
import com.jmunoz.evernote_app.ui.home.HomeActivity;
import com.jmunoz.evernote_app.ui.login.LoginActivity;
import com.jmunoz.evernote_app.util.Constants;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;


/**
 * Simple SplashActivity
 *
 * @author jmunoz
 */
public class SplashActivity extends Activity {

    private final int SPLASH_DISPLAY_LENGHT = 1000;

    public static EvernoteSession evernoteSession;

    public static SplashActivity instance;

    public SplashActivity(){
        instance = this;
    }

    public static SplashActivity getInstance(){
        return instance;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        Thread background = new Thread() {
            public void run() {

                try {
                    sleep(3 * SPLASH_DISPLAY_LENGHT);

//                    Intent i=new Intent(getBaseContext(),LoginActivity.class);
//                    startActivity(i);

                    evernoteSession = EvernoteSession.getInstance(getApplicationContext(), Constants.EvernoteConstants.CONSUMER_KEY, Constants.EvernoteConstants.CONSUMER_SECRET, Constants.EvernoteConstants.EVERNOTE_SERVICE, false);
                    evernoteSession.authenticate(SplashActivity.this);

                } catch (Exception e) {

                }
            }
        };

        background.start();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            // Update UI when oauth activity returns result
            case EvernoteSession.REQUEST_CODE_OAUTH:
                if (resultCode == Activity.RESULT_OK) {
                    // Authentication was successful, do what you need to do in your app
                    Intent i=new Intent(getBaseContext(),HomeActivity.class);
                    startActivity(i);
                    finish();

                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.error_login_title).setMessage(R.string.error_login_message).setCancelable(false).setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            finish();

                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.setCancelable(false);
                    alert.show();
                }
                break;
        }
    }

    public EvernoteSession getEvernoteSession(){
        return evernoteSession;
    }
}
