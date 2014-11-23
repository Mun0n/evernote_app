package com.jmunoz.evernote_app.ui.home;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jmunoz.evernote_app.R;
import com.jmunoz.evernote_app.data.NoteData;

import java.util.ArrayList;

/**
 * Created by jmunoz on 23/11/14.
 */
public class NoteAdapter extends ArrayAdapter<NoteData> {

    private Activity activity;
    private ArrayList<NoteData> noteDatas;
    private LayoutInflater inflater = null;

    public NoteAdapter(Activity activity, int textViewId, ArrayList<NoteData> noteDatas){
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
                vi = inflater.inflate(R.layout.list_adapter_title, parent, false);
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
