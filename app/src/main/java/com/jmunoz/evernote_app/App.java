package com.jmunoz.evernote_app;

import android.app.Application;

import com.evernote.client.android.EvernoteSession;
import com.jmunoz.evernote_app.util.Constants;

/**
 * Created by jmunoz on 18/11/14.
 */
public class App extends Application {


    public static EvernoteSession evernoteSession;


    @Override
    public void onCreate() {
        super.onCreate();
        evernoteSession = EvernoteSession.getInstance(getApplicationContext(), Constants.EvernoteConstants.CONSUMER_KEY, Constants.EvernoteConstants.CONSUMER_SECRET, Constants.EvernoteConstants.EVERNOTE_SERVICE, false);

    }

    public EvernoteSession getEvernoteSession(){
        return evernoteSession;
    }
}
