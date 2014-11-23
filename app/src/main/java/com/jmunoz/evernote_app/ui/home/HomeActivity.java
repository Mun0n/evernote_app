package com.jmunoz.evernote_app.ui.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Outline;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.transport.TTransportException;
import com.jmunoz.evernote_app.App;
import com.jmunoz.evernote_app.BuildConfig;
import com.jmunoz.evernote_app.R;
import com.jmunoz.evernote_app.data.NoteData;
import com.jmunoz.evernote_app.ui.detail.DetailActivity;
import com.jmunoz.evernote_app.ui.splash.SplashActivity;
import com.jmunoz.evernote_app.ui.toolbar.ToolbarActivity;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;

/**
 * Created by jmunoz on 18/11/14.
 */
public class HomeActivity extends ToolbarActivity implements AdapterView.OnItemClickListener {

    private static final int CREATED_NOTE = 200;
    public static final String NOTE_TITLE = "note_title";
    public static final String NOTE_CONTENT = "note_content";

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(android.R.id.list)
    ListView listNotes;

    @InjectView(R.id.add_button)
    FloatingActionButton addButton;

    private App app;
    private ArrayList<NoteData> notesArray;
    private ArrayAdapter<NoteData> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.inject(this);

        app = (App) getApplication();

        notesArray = new ArrayList<NoteData>();
        mAdapter = new TitleArrayAdapter(this, android.R.layout.simple_list_item_1, notesArray);
        listNotes.setEmptyView(findViewById(android.R.id.empty));
        listNotes.setAdapter(mAdapter);
        listNotes.setOnItemClickListener(this);
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
                sortByName(notesArray, mAdapter);
                return true;
            case R.id.action_order_date:
                findNotesByQuery("");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sortByName(ArrayList<NoteData> notesData, ArrayAdapter<NoteData> mAdapter) {
        Collections.sort(notesData, new Comparator<NoteData>() {
            @Override
            public int compare(NoteData noteData, NoteData noteData2) {
                String s1 = noteData.getTitle();
                String s2 = noteData2.getTitle();
                return s1.compareToIgnoreCase(s2);
            }
        });
        mAdapter.notifyDataSetChanged();

    }

    public void findNotesByQuery(String query) {
        final int offset = 0;
        final int pageSize = 40;

        final NoteFilter filter = new NoteFilter();
        filter.setOrder(NoteSortOrder.UPDATED.getValue());
        filter.setWords(query);
        final NotesMetadataResultSpec spec = new NotesMetadataResultSpec();
        spec.setIncludeTitle(true);

        mAdapter.clear();

        try {
            // Callback invoked asynchronously from the notes search.  Factored out here
            // so that it can be reused for a local or linked notebook search below
            final OnClientCallback<NotesMetadataList> callback = new OnClientCallback<NotesMetadataList>() {
                @Override
                public void onSuccess(NotesMetadataList data) {
                    Toast.makeText(getApplicationContext(), R.string.notes_searched, Toast.LENGTH_LONG).show();

                    for (NoteMetadata note : data.getNotes()) {
                        String title = note.getTitle();
                        NoteData dataNote = new NoteData(title, note.getGuid());
                        notesArray.add(dataNote);
                    }
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onException(Exception exception) {
                    onError(exception, "Error listing notes. ", R.string.error_listing_notes);
                }
            };


            if (!app.getEvernoteSession().isAppLinkedNotebook()) {
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
        } catch (TTransportException exception) {
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

    public void onError(Exception exception, String logstr, int id) {
        Log.e("TAG", logstr + exception);
        Toast.makeText(getApplicationContext(), id, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String id = notesArray.get(i).getId();
        try {
            app.getEvernoteSession().getClientFactory().createNoteStoreClient().getNote(id, true, false, false, false, new OnClientCallback<Note>() {
                @Override
                public void onSuccess(Note data) {
                    Intent i = new Intent(getBaseContext(), DetailActivity.class);
                    i.putExtra(NOTE_TITLE, data.getTitle());
                    i.putExtra(NOTE_CONTENT, data.getContent());
                    startActivity(i);
                }

                @Override
                public void onException(Exception exception) {
                    onError(exception, "Error creating recovering note. ", R.string.error_creating_notestore);
                }
            });
        } catch (TTransportException e) {
            e.printStackTrace();
        }

    }

    @OnClick(R.id.add_button)
    public void onAddNoteClicked() {
        Intent i = new Intent(getBaseContext(), DetailActivity.class);
        startActivityForResult(i, CREATED_NOTE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATED_NOTE && resultCode == RESULT_OK) {
            findNotesByQuery("");
        }
    }

    private class TitleArrayAdapter extends ArrayAdapter<NoteData>{

        private Activity activity;
        private ArrayList<NoteData> noteDatas;
        private LayoutInflater inflater = null;

        public TitleArrayAdapter(Activity activity, int textViewId, ArrayList<NoteData> noteDatas){
            super(activity, textViewId, noteDatas);
            this.activity = activity;
            this.noteDatas = noteDatas;

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return noteDatas.size();
        }

        public NoteData getItem(NoteData position){
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public class ViewHolder{
            public TextView display_name;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            final ViewHolder holder;
            try {
                if (convertView == null) {
                    vi = inflater.inflate(R.layout.list_adapter_title, null);
                    holder = new ViewHolder();

                    holder.display_name = (TextView) vi.findViewById(R.id.title);

                    vi.setTag(holder);
                } else {
                    holder = (ViewHolder) vi.getTag();
                }



                holder.display_name.setText(noteDatas.get(position).getTitle());


            } catch (Exception e) {


            }
            return vi;
        }
    }

}
