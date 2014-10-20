package pl.edu.agh.pp.extasks.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import pl.edu.agh.pp.extasks.framework.Note;
import pl.edu.agh.pp.extasks.framework.TasksProvider;
import pl.edu.agh.pp.extasks.framework.TrelloProvider;

/**Main activity of ExTasks application. It allows to connect to specified services and recieve notes from them.
 * @author Jakub Lasisz
 * @author Maciej Sipko
 */
public class MainActivity extends SherlockFragmentActivity implements ActionBar.TabListener, AddNoteDialogFragment.NoticeDialogListener {

    static final String TAG = MainActivity.class.getSimpleName();

    private final Handler handler = new Handler();
    private List<Note> noteLists = new LinkedList<Note>();
    private boolean useLogo = false;
    private boolean showHomeUp = false;
    private TasksProvider trelloProvider;

    /**
     * Updates the current note list, used by ConnectionAsyncTask class.
     * @param newList The new note list
     */
    public void updateNoteList(List<Note> newList) {
        noteLists = newList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final ActionBar ab = getSupportActionBar();
        setupActionBar(ab);

        // default to tab navigation
        showTabsNav();

        TextView tv = (TextView) findViewById(R.id.tasksList);
        tv.setText("Press refresh button to get your Trello tasks");

    }

    private void setupActionBar(ActionBar ab) {
        // set defaults for logo & home up
        ab.setDisplayHomeAsUpEnabled(showHomeUp);
        ab.setDisplayUseLogoEnabled(useLogo);

        // set up tabs nav
        if (noteLists.size() == 0) {
            ab.addTab(ab.newTab().setText("Empty tab").setTabListener(this));
        }

        // set up list nav
        ab.setListNavigationCallbacks(ArrayAdapter
                        .createFromResource(this, R.array.sections,
                                R.layout.sherlock_spinner_dropdown_item),
                new ActionBar.OnNavigationListener() {
                    public boolean onNavigationItemSelected(int itemPosition,
                                                            long itemId) {
                        return false;
                    }
                }
        );
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Dialog dialogView = dialog.getDialog();
        EditText editTextName = (EditText) dialogView.findViewById(R.id.noteName);
        EditText editText = (EditText) dialogView.findViewById(R.id.noteText);

        TextView listID = (TextView) dialogView.findViewById(R.id.chosenListID);

        new AddNoteAsyncTask(MainActivity.this, trelloProvider).execute(editTextName.getText().toString(), editText.getText().toString(), listID.getText().toString());
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main_menu, menu);

        // set up a listener for the refresh item
        final MenuItem refresh = menu.findItem(R.id.menu_refresh);
        refresh.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            // on selecting show progress spinner for 1s
            public boolean onMenuItemClick(MenuItem item) {
                // item.setActionView(R.layout.progress_action);
                handler.postDelayed(new Runnable() {
                    public void run() {
                        refresh.setActionView(null);
                    }
                }, 1000);
                return false;
            }
        });

        final MenuItem settings = menu.findItem(R.id.add_note);
        settings.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Log.d(TAG, "klikłem");
                AddNoteDialogFragment dialog = new AddNoteDialogFragment();
                dialog.setFragmentManager(getSupportFragmentManager());
                dialog.setTrelloProvider(trelloProvider);
                dialog.show(getSupportFragmentManager(), "Add Note Dialog");
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
       // cm.requestRouteToHost(ConnectivityManager.TYPE_WIFI, )
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                // switch to a progress animation
                if (isOnline()) {
                    item.setActionView(R.layout.indeterminate_progress_action);
                    String value;
                    try {
                        noteLists = new LinkedList<Note>();
                        trelloProvider = new TrelloProvider("c74be1bc4cc64e0eb21aa8cd68067c11", "1cebce0d98eb0fc5a8fda7fecd5725aa500bcdb35edf7915d46453b8c7d38f3a");
                        value = new ConnectionAsyncTask(this, trelloProvider).execute().get();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "InterruptedException at onOptionsItemSelected", e);
                        return false;
                    } catch (ExecutionException e) {
                        Log.e(TAG, "ExecutionException at onOptionsItemSelected", e);
                        return false;
                    }
                    if (value != null) {
                        Log.d(TAG, "zwracam true");
                        refreshTabs();
                        refreshTextView();
                        return true;
                    } else
                        return false;
                } else
                {
                    new AlertDialog.Builder(MainActivity.this.getApplicationContext())
                            .setTitle("INTERNET CONNECTION ERROR")
                            .setMessage("Turn on your Internet connection and try again.")
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            finish();
                                        }
                                    }).create();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Refreshes currently selected tab.
     */
    private void refreshTextView() {
        TextView tv = (TextView) findViewById(R.id.tasksList);
        int whichNo = getActionBar().getSelectedTab().getPosition();
        Note note = noteLists.get(whichNo);
        tv.setText(note.getText());
//        for (Note n : taskList) {
//            tv.append(n.getText() + "\n");
//        }
    }

    /**
     * Refreshes all tabs in activity.
     */
    private void refreshTabs() {
        ActionBar ab = getSupportActionBar();
        int size = noteLists.size();
        ab.removeAllTabs();
        for (int i = 0; i < size; i++) {
            ab.addTab(ab.newTab().setText(noteLists.get(i).getTitle()).setTabListener(this));
        }
    }

    /**
     * Makes a tab navigation bar visible.
     */
    private void showTabsNav() {
        ActionBar ab = getSupportActionBar();
        if (ab.getNavigationMode() != ActionBar.NAVIGATION_MODE_TABS) {
            ab.setDisplayShowTitleEnabled(false);
            ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
    }

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        if (noteLists.size() > 0) {
            refreshTextView();
        }
    }

    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        // FIXME implement this
    }

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        // FIXME implement this
    }

    public void chooseList(View view) {
        ChooseListDialog dialog = new ChooseListDialog();
        Map<String, org.trello4j.model.List> map = ((TrelloProvider) trelloProvider).getLists();
        ChooseListDialog dialog2 = dialog.newInstance(map.keySet().toArray(new String[map.keySet().size()]));
        dialog2.setItemsMap(((TrelloProvider) trelloProvider).getLists());
        dialog2.show(getSupportFragmentManager(), "Choose dialog");
    }
}