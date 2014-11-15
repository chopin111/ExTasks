package pl.edu.agh.pp.extasks.app;

/**
 * Created by chopi_000 on 2014-11-15.
 */

import android.os.AsyncTask;

import pl.edu.agh.pp.extasks.framework.TasksProvider;

public class RemoveNoteAsyncTask extends AsyncTask<String, String, String> {

    private MainActivity activity;
    /**
     * Task provider which is called to create a connection and return notes.
     */
    private TasksProvider provider;

    public RemoveNoteAsyncTask(MainActivity activity, TasksProvider provider) {
        super();
        this.activity = activity;
        this.provider = provider;
    }

    @Override
    protected String doInBackground(String... strings) {
        provider.removeNote(strings[0]);
        return "";
    }
}

