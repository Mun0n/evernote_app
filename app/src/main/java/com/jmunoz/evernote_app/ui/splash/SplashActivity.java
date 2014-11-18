package com.jmunoz.evernote_app.ui.splash;

import com.evernote.client.android.EvernoteSession;
import com.jmunoz.evernote_app.App;
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
                    App app = (App) getApplication();
                    EvernoteSession evernoteSession = app.getEvernoteSession();
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
        switch (requestCode) {
            // Update UI when oauth activity returns result
            case EvernoteSession.REQUEST_CODE_OAUTH:
                if (resultCode == Activity.RESULT_OK) {
                    // Authentication was successful, do what you need to do in your app
                    Intent i = new Intent(getBaseContext(), HomeActivity.class);
                    startActivity(i);
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("Error en login")
                            .setMessage("No ha podido realizarse el login en Evernote, intentelo de nuevo más tarde")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                finish();
                break;
        }
    }

}
