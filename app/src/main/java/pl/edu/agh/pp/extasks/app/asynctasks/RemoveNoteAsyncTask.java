package pl.edu.agh.pp.extasks.app.asynctasks;

/**
 * Created by chopi_000 on 2014-11-15.
 */

import android.os.AsyncTask;

import pl.edu.agh.pp.extasks.app.MainActivity;
import pl.edu.agh.pp.extasks.framework.exception.SynchronizationException;
import pl.edu.agh.pp.extasks.framework.TasksProvider;

public class RemoveNoteAsyncTask extends AsyncTask<String, String, String> {

    private MainActivity activity;
    /**
     * Task provider which is called to remove specific note.
     */
    private TasksProvider provider;

    public RemoveNoteAsyncTask(MainActivity activity, TasksProvider provider) {
        super();
        this.activity = activity;
        this.provider = provider;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            provider.removeNote(strings[0]);
        } catch (SynchronizationException e) {
            e.printStackTrace();
        }
        return "";
    }
}

