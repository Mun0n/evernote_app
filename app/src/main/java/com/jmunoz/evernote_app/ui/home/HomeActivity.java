package com.jmunoz.evernote_app.ui.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Outline;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.evernote.client.android.AsyncLinkedNoteStoreClient;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.transport.TTransportException;
import com.jmunoz.evernote_app.App;
import com.jmunoz.evernote_app.BuildConfig;
import com.jmunoz.evernote_app.R;
import com.jmunoz.evernote_app.ui.splash.SplashActivity;
import com.jmunoz.evernote_app.ui.toolbar.ToolbarActivity;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by jmunoz on 18/11/14.
 */
public class HomeActivity extends ToolbarActivity {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(android.R.id.list)
    ListView listNotes;

    @InjectView(R.id.add_button)
    FloatingActionButton addButton;

    private App app;
    private ArrayList<String> notesNames;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.inject(this);

        app = (App) getApplication();

        notesNames = new ArrayList();
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, notesNames);
        listNotes.setEmptyView(findViewById(android.R.id.empty));
        listNotes.setAdapter(mAdapter);
        addButton.attachToListView(listNotes);

        findNotesByQuery("");

    }

    private void showError() {
        new AlertDialog.Builder(this)
                .setTitle("Error en home")
                .setMessage("No han podido cargarse las notas. Intentelo de nuevo más tarde")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_order_title:
                return true;
            case R.id.action_order_date:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void findNotesByQuery(String query) {
        final int offset = 0;
        final int pageSize = 10;

        final NoteFilter filter = new NoteFilter();
        filter.setOrder(NoteSortOrder.UPDATED.getValue());
        filter.setWords(query);
        final NotesMetadataResultSpec spec = new NotesMetadataResultSpec();
        spec.setIncludeTitle(true);

        mAdapter.clear();

        try{
            // Callback invoked asynchronously from the notes search.  Factored out here
            // so that it can be reused for a local or linked notebook search below
            final OnClientCallback<NotesMetadataList> callback = new OnClientCallback<NotesMetadataList>() {
                @Override
                public void onSuccess(NotesMetadataList data) {
                    Toast.makeText(getApplicationContext(), R.string.notes_searched, Toast.LENGTH_LONG).show();

                    for (NoteMetadata note : data.getNotes()) {
                        String title = note.getTitle();
                        notesNames.add(title);
                    }
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onException(Exception exception) {
                    onError(exception, "Error listing notes. ", R.string.error_listing_notes);
                }
            };


            if(!app.getEvernoteSession().isAppLinkedNotebook()) {
                // Normal, local notebook search
                app.getEvernoteSession().getClientFactory().createNoteStoreClient()
                        .findNotesMetadata(filter, offset, pageSize, spec, callback);
            } else {
                // Linked notebook search
                invokeOnAppLinkedNotebook(new OnClientCallback<Pair<AsyncLinkedNoteStoreClient, LinkedNotebook>>() {
                    @Override
                    public void onSuccess(Pair<AsyncLinkedNoteStoreClient, LinkedNotebook> pair) {
                        pair.first.findNotesMetadataAsync(filter, offset, pageSize, spec, callback);
                    }

                    @Override
                    public void onException(Exception exception) {
                        callback.onException(exception);
                    }
                });
            }
        } catch (TTransportException exception){
            onError(exception, "Error creating notestore. ", R.string.error_creating_notestore);
        }
    }

    protected void invokeOnAppLinkedNotebook(final OnClientCallback<Pair<AsyncLinkedNoteStoreClient, LinkedNotebook>> callback) {
        try {
            // We need to get the one and only linked notebook
            app.getEvernoteSession().getClientFactory().createNoteStoreClient().listLinkedNotebooks(new OnClientCallback<List<LinkedNotebook>>() {
                @Override
                public void onSuccess(List<LinkedNotebook> linkedNotebooks) {
                    // We should only have one linked notebook
                    if (linkedNotebooks.size() != 1) {
                        Log.e("TAG", "Error getting linked notebook - more than one linked notebook");
                        callback.onException(new Exception("Not single linked notebook"));
                    } else {
                        final LinkedNotebook linkedNotebook = linkedNotebooks.get(0);
                        app.getEvernoteSession().getClientFactory().createLinkedNoteStoreClientAsync(linkedNotebook, new OnClientCallback<AsyncLinkedNoteStoreClient>() {
                            @Override
                            public void onSuccess(AsyncLinkedNoteStoreClient asyncLinkedNoteStoreClient) {
                                // Finally create the note in the linked notebook
                                callback.onSuccess(new Pair<AsyncLinkedNoteStoreClient, LinkedNotebook>(asyncLinkedNoteStoreClient, linkedNotebook));
                            }

                            @Override
                            public void onException(Exception exception) {
                                callback.onException(exception);
                            }
                        });
                    }
                }

                @Override
                public void onException(Exception exception) {
                    callback.onException(exception);
                }
            });
        } catch (TTransportException exception) {
            callback.onException(exception);
        }
    }

    public void onError(Exception exception, String logstr, int id){
        Log.e("TAG", logstr + exception);
        Toast.makeText(getApplicationContext(), id, Toast.LENGTH_LONG).show();
    }
}
