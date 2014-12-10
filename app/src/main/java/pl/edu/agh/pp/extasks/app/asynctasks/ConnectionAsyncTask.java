package pl.edu.agh.pp.extasks.app.asynctasks;

import android.os.AsyncTask;

import java.util.List;
import java.util.concurrent.ExecutionException;

import pl.edu.agh.pp.extasks.app.MainActivity;
import pl.edu.agh.pp.extasks.framework.exception.InitializationException;
import pl.edu.agh.pp.extasks.framework.model.Note;
import pl.edu.agh.pp.extasks.framework.model.NoteList;
import pl.edu.agh.pp.extasks.framework.TasksProvider;

/**
 * AsyncTask which delegates creating connection with a TaskProvider, recieves all the notes and returns them to the ExTasksActivity.
 *
 * @author Jakub Lasisz
 * @author Maciej Sipko
 */
public class ConnectionAsyncTask extends AsyncTask<String, String, String> {
    /**
     * Activity which will recieve note lists from provider.
     */
    private MainActivity activity;
    /**
     * Task provider which is called to create a connection and return notes.
     */
    private TasksProvider provider;

    /**
     * Connection async task for given provider
     * @param activity   source activity
     * @param provider   provider
     */
    public ConnectionAsyncTask(MainActivity activity, TasksProvider provider) {
        super();
        this.activity = activity;
        this.provider = provider;
    }

    @Override
    protected String doInBackground(String... strings) {
        try{
            authenticate();
        } catch (InitializationException e) {
            e.printStackTrace();
            return e.getMessage();
        }
        List<Note> noteLists = provider.getNotes();
        activity.updateNoteList(noteLists);
        List<NoteList> boardsMap = provider.getBoards();
        activity.updateBoardsMap(boardsMap);

        return "";
    }

    /**
     * Performs authentication for current provider
     */
    private void authenticate() throws InitializationException{
        provider.authenticate();
        provider.initialize();
        provider.getNotesFromService();
    }
}