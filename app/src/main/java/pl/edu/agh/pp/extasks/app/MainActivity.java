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
import android.view.LayoutInflater;
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
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import pl.edu.agh.pp.extasks.framework.Note;
import pl.edu.agh.pp.extasks.framework.NoteList;
import pl.edu.agh.pp.extasks.framework.TasksProvider;
import pl.edu.agh.pp.extasks.framework.TodoistProvider;
import pl.edu.agh.pp.extasks.framework.TrelloProvider;

/**
 * Main activity of ExTasks application. It allows to connect to specified services and recieve notes from them.
 *
 * @author Jakub Lasisz
 * @author Maciej Sipko
 */
public class MainActivity extends SherlockFragmentActivity implements ActionBar.TabListener, AddNoteDialogFragment.NoticeDialogListener, ChooseListDialog.chooseListInt {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final Handler handler = new Handler();
    private final boolean useLogo = false;
    private final boolean showHomeUp = false;
    private final Map<String, NoteList> listByNamesMap = new TreeMap<>();
    private final List<NoteList> boards = new ArrayList<>();
    private List<Note> noteLists = new LinkedList<Note>();
    private TasksProvider trelloProvider;
    private TasksProvider todoistProvider;
    private TasksProvider chosenProvider;
    private String chosenListID;
    private NoteList chosenBoard;

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
        final ListView lv = (ListView) findViewById(R.id.tasksListView);

        final String[] values = {"Press refresh to update lists"};

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, values);
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
        final Dialog dialogView = dialog.getDialog();
        final EditText editTextName = (EditText) dialogView.findViewById(R.id.noteName);
        final EditText editText = (EditText) dialogView.findViewById(R.id.noteText);

        final String listID = chosenListID;
        new AddNoteAsyncTask(MainActivity.this, chosenProvider).execute(editTextName.getText().toString(), editText.getText().toString(), listID);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    @Override
    public void onListChoose(String listName, String listID) {
        chosenListID = listID;
        chosenProvider = listByNamesMap.get(listName).getProvider();
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
                AddNoteDialogFragment dialog = new AddNoteDialogFragment();
                dialog.show(getSupportFragmentManager(), "Add Note Dialog");
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public boolean isOnline() {
        final ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
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
                        boards.clear();
                        trelloProvider = new TrelloProvider("c74be1bc4cc64e0eb21aa8cd68067c11", "1cebce0d98eb0fc5a8fda7fecd5725aa500bcdb35edf7915d46453b8c7d38f3a");
                        todoistProvider = new TodoistProvider("jakublasisz@gmail.com", "iamalazybastard");
                        value = new ConnectionAsyncTask(this, trelloProvider).execute().get();
                        new ConnectionAsyncTask(this, todoistProvider).execute().get();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "InterruptedException at onOptionsItemSelected", e);
                        return false;
                    } catch (ExecutionException e) {
                        Log.e(TAG, "ExecutionException at onOptionsItemSelected", e);
                        return false;
                    }
                    if (value != null) {
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
        final ListView lv = (ListView) findViewById(R.id.tasksListView);
        android.app.ActionBar ab = getActionBar();
        int tabNo = 0;
        if (ab != null) {
            android.app.ActionBar.Tab tab = ab.getSelectedTab();
            if (tab != null) {
                tabNo = tab.getPosition();
            }
        }
        chosenBoard = boards.get(tabNo);
        final List<Note> chosenBoardNotes = chosenBoard.getNotes();

        final List<String> values = createValues(chosenBoardNotes);

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, values);
        lv.setAdapter(adapter);
        registerForContextMenu(lv);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.tasksListView) {
            final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(chosenBoard.getName());
            final String[] menuItems = getResources().getStringArray(R.array.listview_options);
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        getMenuDesc(item);

        refreshTextView();
        return true;
    }

    public void getMenuDesc(android.view.MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        String[] menuItems = getResources().getStringArray(R.array.listview_options);
        String name = menuItems[menuItemIndex];
        doStuff(name, info);
    }

    public void doStuff(String name, AdapterView.AdapterContextMenuInfo info) {
        if (name.equals("Delete Note")) {
            String cardId = chosenBoard.get(info.position).getId();
            new RemoveNoteAsyncTask(MainActivity.this, chosenBoard.getProvider()).execute(cardId);
        } else if (name.equals("Edit Note")) {
            final String cardId = chosenBoard.get(info.position).getId();
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = getLayoutInflater();

            builder.setView(inflater.inflate(R.layout.dialog_editnote, null))
                    .setMessage("Save note")
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            EditText editTextName = (EditText) ((Dialog) dialog).findViewById(R.id.editNoteName);
                            EditText editText = (EditText) ((Dialog) dialog).findViewById(R.id.editNoteText);

                            new EditNoteAsyncTask(MainActivity.this, chosenBoard.getProvider()).execute(cardId, editTextName.getText().toString(), editText.getText().toString());
                            refreshTextView();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            // Create the AlertDialog object and return it
            builder.show();
        }
    }

    /**
     * Refreshes all tabs in activity.
     */
    private void refreshTabs() {
        final ActionBar ab = getSupportActionBar();
        final int size = boards.size();
        ab.removeAllTabs();
        for (int i = 0; i < size; i++) {
            ab.addTab(ab.newTab().setText(boards.get(i).getName()).setTabListener(this));
        }
    }

    /**
     * Makes a tab navigation bar visible.
     */
    private void showTabsNav() {
        final ActionBar ab = getSupportActionBar();
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
        listByNamesMap.putAll(trelloProvider.getListsMap());
        listByNamesMap.putAll(todoistProvider.getListsMap());
        final ChooseListDialog dialog = ChooseListDialog.newInstance(listByNamesMap.keySet().toArray(new String[listByNamesMap.size()]));
        dialog.appendItemsMap(listByNamesMap);
        dialog.show(getSupportFragmentManager(), "Choose dialog");
    }

    public void updateBoardsMap(List<NoteList> boards) {
        this.boards.addAll(boards);
    }

    private List<String> createValues(List<Note> chosenBoardNotes) {
        List<String> values = new ArrayList<>(chosenBoardNotes.size());
        for (Note n : chosenBoardNotes) {
            values.add(n.getTitle());
        }
        return values;
    }


}