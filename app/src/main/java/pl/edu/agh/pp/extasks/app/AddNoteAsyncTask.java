package pl.edu.agh.pp.extasks.app;

import android.os.AsyncTask;

import pl.edu.agh.pp.extasks.framework.Note;
import pl.edu.agh.pp.extasks.framework.TasksProvider;

public class AddNoteAsyncTask extends AsyncTask<String, String, String> {

    private MainActivity activity;
    /**
     * Task provider which is called to create a connection and return notes.
     */
    private TasksProvider provider;

    public AddNoteAsyncTask(MainActivity activity, TasksProvider provider) {
        super();
        this.activity = activity;
        this.provider = provider;
    }

    @Override
    protected String doInBackground(String... strings) {
        provider.addNote("title", "text");
        return "";
    }
}
