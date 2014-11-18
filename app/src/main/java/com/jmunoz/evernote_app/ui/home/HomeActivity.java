package com.jmunoz.evernote_app.ui.home;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.jmunoz.evernote_app.App;
import com.jmunoz.evernote_app.R;
import com.jmunoz.evernote_app.ui.splash.SplashActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by jmunoz on 18/11/14.
 */
public class HomeActivity extends Activity {

    @InjectView(R.id.homeText)
    public TextView homeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.inject(this);
        updateAuthUi();
    }

    private void updateAuthUi() {
        //show login button if logged out
        App app = (App) getApplication();
        if(app.getEvernoteSession().isLoggedIn()) {
            homeTextView.setText("ESTA LOGEADO!");
        }else{
            //MALA COSA
            homeTextView.setText("NO ESTA LOGEADO!");
        }
        //Show logout button if logged in
//    mLogoutButton.setEnabled(mEvernoteSession.isLoggedIn());
    }
}
