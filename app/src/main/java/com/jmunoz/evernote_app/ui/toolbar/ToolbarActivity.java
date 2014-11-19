package com.jmunoz.evernote_app.ui.toolbar;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.jmunoz.evernote_app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by javier.munoz on 19/11/14.
 */
public class ToolbarActivity extends ActionBarActivity {

    ViewGroup toolbarContent;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;


    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.toolbar_activity);
        toolbarContent = ButterKnife.findById(this,R.id.toolbarContent);
        getLayoutInflater().inflate(layoutResID, toolbarContent);
        ButterKnife.inject(this);
        initToolBar();
    }

    private void initToolBar() {
        setSupportActionBar(toolbar);
    }

}
