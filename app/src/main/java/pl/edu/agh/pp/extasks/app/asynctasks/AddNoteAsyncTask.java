package pl.edu.agh.pp.extasks.app.asynctasks;

import android.os.AsyncTask;

import pl.edu.agh.pp.extasks.app.MainActivity;
import pl.edu.agh.pp.extasks.framework.exception.SynchronizationException;
import pl.edu.agh.pp.extasks.framework.TasksProvider;

public class AddNoteAsyncTask extends AsyncTask<String, String, String> {

    private MainActivity activity;
    /**
     * Task provider which is called to create a connection and return notes.
     */
    private TasksProvider provider;

    /**
     * Adding notes to given provider
     * @param activity   source activity
     * @param provider   provider
     */
    public AddNoteAsyncTask(MainActivity activity, TasksProvider provider) {
        super();
        this.activity = activity;
        this.provider = provider;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            provider.addNote(strings[0], strings[1], strings[2]);
        } catch (SynchronizationException e) {
            e.printStackTrace();
        }
        return "";
    }
}
