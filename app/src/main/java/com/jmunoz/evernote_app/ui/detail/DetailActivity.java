package com.jmunoz.evernote_app.ui.detail;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.Note;
import com.evernote.thrift.transport.TTransportException;
import com.jmunoz.evernote_app.App;
import com.jmunoz.evernote_app.R;
import com.jmunoz.evernote_app.ui.home.HomeActivity;
import com.jmunoz.evernote_app.ui.toolbar.ToolbarActivity;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by jmunoz on 22/11/14.
 */
public class DetailActivity extends ToolbarActivity implements GestureOverlayView.OnGesturePerformedListener {

    @InjectView(R.id.titleDetail)
    public EditText titleEditText;

    @InjectView(R.id.cotentDetail)
    public EditText contentEditText;

    @InjectView(R.id.gestures)
    public GestureOverlayView gesturesView;

    private Menu menu;

    private App app;
    private String title, content;

    GestureLibrary gLibrary;
    GestureOverlayView mView;
    String txtToDisplay = "";

    private boolean focusTitle = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);
        ButterKnife.inject(this);

        if (getIntent().getExtras() != null) {
            title = getIntent().getExtras().getString(HomeActivity.NOTE_TITLE);
            content = getIntent().getExtras().getString(HomeActivity.NOTE_CONTENT);

            titleEditText.setText(title);
            titleEditText.setEnabled(false);
            contentEditText.setText(Html.fromHtml(content));
            contentEditText.setEnabled(false);
        }else{
            selectInputMode();
        }

        app = (App) getApplication();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        if (getIntent().getExtras() == null) {
            inflater.inflate(R.menu.detail_activity_actions, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {

            switch (item.getItemId()) {
                case R.id.action_add:
                    createNote(titleEditText.getText().toString(), contentEditText.getText().toString());
                    return true;
                case R.id.action_change_focus:

                    if (focusTitle) {
                        txtToDisplay = contentEditText.getText().toString();
                        focusTitle = false;
                        contentEditText.requestFocus();
                    } else {
                        txtToDisplay = titleEditText.getText().toString();
                        focusTitle = true;
                        titleEditText.requestFocus();
                    }
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }

        } catch (TTransportException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void selectInputMode() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_method_title)
                .setMessage(R.string.dialog_method_text)
                .setPositiveButton(R.string.keyboard, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        gesturesView.setVisibility(View.GONE);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.gestures, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        gLibrary = GestureLibraries.fromRawResource(DetailActivity.this, R.raw.abc);
                        if (gLibrary != null) {
                            if (!gLibrary.load()) {
                                Log.e("GestureSample", "Gesture library was not loaded…");
                                finish();
                            } else {
                                mView = (GestureOverlayView) findViewById(R.id.gestures);
                                mView.addOnGesturePerformedListener(DetailActivity.this);
                            }
                        }
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    public void createNote(String title, String content) throws TTransportException {
        if (app.getEvernoteSession().isLoggedIn()) {
            Note note = new Note();
            note.setTitle(title);
            note.setContent(EvernoteUtil.NOTE_PREFIX + content + EvernoteUtil.NOTE_SUFFIX);
            app.getEvernoteSession().getClientFactory().createNoteStoreClient().createNote(note, new OnClientCallback<Note>() {
                @Override
                public void onSuccess(final Note data) {
                    setResult(RESULT_OK);
                    finish();
                    Toast.makeText(getApplicationContext(), data.getTitle() + " has been created", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onException(Exception exception) {
                    Toast.makeText(getApplicationContext(), "No se creó la nota", Toast.LENGTH_SHORT).show();
                    Log.e("EVERNOTE", "Error creating note", exception);
                }
            });
        }
    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        ArrayList<Prediction> predictions = gLibrary.recognize(gesture);
        // one prediction needed
        if (predictions.size() > 0) {
            Prediction prediction = predictions.get(0);
            // checking prediction
            if (prediction.score > 1.0) {
                txtToDisplay += prediction.name;
                if (focusTitle) {
                    titleEditText.setText(txtToDisplay);
                } else {
                    contentEditText.setText(txtToDisplay);
                }
            }
        }
    }

}
