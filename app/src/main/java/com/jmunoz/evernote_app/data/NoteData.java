package com.jmunoz.evernote_app.data;

/**
 * Created by jmunoz on 23/11/14.
 */
public class NoteData {

    private String title;
    private String id;

    public NoteData(String title, String id){
        this.title = title;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

}
