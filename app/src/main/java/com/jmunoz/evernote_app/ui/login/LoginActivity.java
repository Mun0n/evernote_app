package com.jmunoz.evernote_app.ui.login;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.evernote.client.android.EvernoteSession;
import com.jmunoz.evernote_app.R;
import com.jmunoz.evernote_app.util.Constants;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by jmunoz on 17/11/14.
 */
public class LoginActivity extends Activity {

    @InjectView(R.id.loginButton)
    public Button loginButton;

    public static EvernoteSession evernoteSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);

        evernoteSession = EvernoteSession.getInstance(getApplicationContext(), Constants.EvernoteConstants.CONSUMER_KEY, Constants.EvernoteConstants.CONSUMER_SECRET, Constants.EvernoteConstants.EVERNOTE_SERVICE, false);


    }

    @OnClick(R.id.loginButton)
    public void onLoginClick(){
        evernoteSession.authenticate(this);
    }
}
