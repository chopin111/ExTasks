package pl.edu.agh.pp.extasks.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import org.trello4j.model.Board;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import pl.edu.agh.pp.extasks.framework.Note;
import pl.edu.agh.pp.extasks.framework.TasksProvider;
import pl.edu.agh.pp.extasks.framework.TrelloProvider;

/**
 * Main activity of ExTasks application. It allows to connect to specified services and recieve notes from them.
 *
 * @author Jakub Lasisz
 * @author Maciej Sipko
 */
public class MainActivity extends SherlockFragmentActivity implements ActionBar.TabListener, AddNoteDialogFragment.NoticeDialogListener, ChooseListDialog.chooseListInt {

    static final String TAG = MainActivity.class.getSimpleName();

    private final Handler handler = new Handler();
    private List<Note> noteLists = new LinkedList<Note>();
    private boolean useLogo = false;
    private boolean showHomeUp = false;
    private TasksProvider trelloProvider;
    private String chosenListID;
    private Map<Board, List<Note>> boardsMap;
    private Board chosenBoard;

    /**
     * Updates the current note list, used by ConnectionAsyncTask class.
     *
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
        ListView lv = (ListView) findViewById(R.id.tasksListView);

        String[] values = new String[1];
        values[0] = "Press refresh to update lists";

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, values);
        lv.setAdapter(adapter);
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

        String listID = chosenListID;

        new AddNoteAsyncTask(MainActivity.this, trelloProvider).execute(editTextName.getText().toString(), editText.getText().toString(), listID);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    @Override
    public void onListChoose(String listName, String listID) {
        chosenListID = listID.substring(5);

        /*View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_addnote, null);
        TextView listNameDialog = (TextView) view.findViewById(R.id.chosenListName);
        TextView listIDDialog = (TextView) view.findViewById(R.id.chosenListID);
        listNameDialog.setText(listName);
        listIDDialog.setText(listID);

        listNameDialog.invalidate();
        listIDDialog.invalidate();*/

        //cosiek.getDialog().setContentView(view);
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
                } else {
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
        ListView lv = (ListView) findViewById(R.id.tasksListView);
        android.app.ActionBar ab = getActionBar();
        int tabNo = 0;
        if (ab != null) {
            android.app.ActionBar.Tab tab = ab.getSelectedTab();
            if (tab != null) {
                tabNo = tab.getPosition();
            }
        }
        chosenBoard = (Board) boardsMap.keySet().toArray()[tabNo];
        List<Note> chosenBoardNotes = boardsMap.get(chosenBoard);

        List<String> values = createValues(chosenBoardNotes);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, values);
        lv.setAdapter(adapter);
        registerForContextMenu(lv);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.tasksListView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle(boardsMap.get(chosenBoard).get(info.position).getTitle());
            String[] menuItems = getResources().getStringArray(R.array.listview_options);
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String name = getMenuDesc(item);
        if (name.equals("Delete Note")) {
            String cardId = boardsMap.get(chosenBoard).get(info.position).getId();
            new RemoveNoteAsyncTask(MainActivity.this, trelloProvider).execute(cardId);
        }
        refreshTextView();
        return true;
    }

    public String getMenuDesc(android.view.MenuItem item) {
        int menuItemIndex = item.getItemId();
        String[] menuItems = getResources().getStringArray(R.array.listview_options);
        String menuItemName = menuItems[menuItemIndex];
        return menuItemName;
    }

    private List<String> createValues(List<Note> chosenBoardNotes) {
        List<String> values = new ArrayList<String>(chosenBoardNotes.size());
        for (Note n : chosenBoardNotes) {
            values.add(n.getTitle());
        }
        return values;
    }

    /**
     * Refreshes all tabs in activity.
     */
    private void refreshTabs() {
        ActionBar ab = getSupportActionBar();
        int size = boardsMap.size();
        ab.removeAllTabs();
        for (int i = 0; i < size; i++) {
            ab.addTab(ab.newTab().setText(((Board) boardsMap.keySet().toArray()[i]).getName()).setTabListener(this));
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
        //ChooseListDialog dialog = new ChooseListDialog();
        Map<String, org.trello4j.model.List> map = ((TrelloProvider) trelloProvider).getLists();
        ChooseListDialog dialog2 = ChooseListDialog.newInstance(map.keySet().toArray(new String[map.keySet().size()]));
        dialog2.setItemsMap(((TrelloProvider) trelloProvider).getLists());
        dialog2.show(getSupportFragmentManager(), "Choose dialog");
    }

    public void updateBoardsMap(Map<Board, List<Note>> boardsMap) {
        this.boardsMap = boardsMap;
    }
}