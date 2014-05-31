package pl.edu.agh.pp.extasks.app;

import android.os.AsyncTask;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import pl.edu.agh.pp.extasks.framework.NoteList;
import pl.edu.agh.pp.extasks.framework.TasksProvider;

/**AsyncTask which delegates creating connection with a TaskProvider, recieves all the notes and returns them to the ExTasksActivity.
 * @author Jakub Lasisz
 * @author Maciej Sipko
 */
public class ConnectionAsyncTask extends AsyncTask<String, String, String> {
    /**
     * Activity which will recieve note lists from provider.
     */
    private SherlockFragmentActivity activity;
    private java.util.List<NoteList> noteLists;
    /**
     * Task provider which is called to create a connection and return notes.
     */
    private TasksProvider provider;

    public ConnectionAsyncTask(SherlockFragmentActivity activity, TasksProvider provider) {
        super();
        this.activity = activity;
        this.provider = provider;
    }

    @Override
    protected String doInBackground(String... strings) {
        //trello
        //API key: c74be1bc4cc64e0eb21aa8cd68067c11
        //token: 1cebce0d98eb0fc5a8fda7fecd5725aa500bcdb35edf7915d46453b8c7d38f3a

        provider.getNotesFromService();
        noteLists = provider.getNotes();
        ((ExTasksActivity) activity).updateNoteList(noteLists);

        return "";
    }

}