package pl.edu.agh.pp.extasks.app;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import pl.edu.agh.pp.extasks.framework.Note;
import pl.edu.agh.pp.extasks.framework.NoteList;
import pl.edu.agh.pp.extasks.framework.TrelloProvider;

/**Main activity of ExTasks application. It allows to connect to specified services and recieve notes from them.
 * @author Jakub Lasisz
 * @author Maciej Sipko
 */
public class ExTasksActivity extends SherlockFragmentActivity implements ActionBar.TabListener {

    static final String TAG = ExTasksActivity.class.getSimpleName();

    private final Handler handler = new Handler();
    private List<NoteList> noteLists = new LinkedList<NoteList>();
    private boolean useLogo = false;
    private boolean showHomeUp = false;

    /**
     * Updates the current note list, used by ConnectionAsyncTask class.
     * @param newList The new note list
     */
    public void updateNoteList(List<NoteList> newList) {
        noteLists = newList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final ActionBar ab = getSupportActionBar();

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

        // default to tab navigation
        showTabsNav();

        TextView tv = (TextView) findViewById(R.id.tasksList);
        tv.setText("Press refresh button to get your Trello tasks");

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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                // switch to a progress animation
                item.setActionView(R.layout.indeterminate_progress_action);
                String value;
                try {
                    value = new ConnectionAsyncTask(this, new TrelloProvider("c74be1bc4cc64e0eb21aa8cd68067c11", "1cebce0d98eb0fc5a8fda7fecd5725aa500bcdb35edf7915d46453b8c7d38f3a")).execute().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    return false;
                }
                if (value != null) {
                    Log.d(TAG, "zwracam true");
                    refreshTabs();
                    refreshTextView();
                    return true;
                } else
                    return false;
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
        List<Note> taskList = noteLists.get(whichNo).getNotes();
        tv.setText("");
        for (Note n : taskList) {
            tv.append(n.getText() + "\n");
        }
    }

    /**
     * Refreshes all tabs in activity.
     */
    private void refreshTabs() {
        ActionBar ab = getSupportActionBar();
        int size = noteLists.size();
        ab.removeAllTabs();
        for (int i = 0; i < size; i++) {
            ab.addTab(ab.newTab().setText(noteLists.get(i).getName()).setTabListener(this));
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

}