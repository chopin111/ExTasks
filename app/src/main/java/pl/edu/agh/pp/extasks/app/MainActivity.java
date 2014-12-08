package pl.edu.agh.pp.extasks.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import pl.edu.agh.pp.extasks.app.asynctasks.AddNoteAsyncTask;
import pl.edu.agh.pp.extasks.app.asynctasks.ConnectionAsyncTask;
import pl.edu.agh.pp.extasks.app.asynctasks.EditNoteAsyncTask;
import pl.edu.agh.pp.extasks.app.asynctasks.RemoveNoteAsyncTask;
import pl.edu.agh.pp.extasks.framework.TasksProvider;
import pl.edu.agh.pp.extasks.framework.TodoistProvider;
import pl.edu.agh.pp.extasks.framework.TrelloProvider;
import pl.edu.agh.pp.extasks.framework.model.Note;
import pl.edu.agh.pp.extasks.framework.model.NoteList;

/**
 * Main activity of ExTasks application. It allows to connect to specified services and recieve notes from them.
 *
 * @author Jakub Lasisz
 * @author Maciej Sipko
 */
public class MainActivity extends SherlockFragmentActivity implements ActionBar.TabListener, AddNoteDialogFragment.NoticeDialogListener, ChooseListDialog.chooseListInt {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final Handler handler = new Handler();
    private final Map<String, NoteList> listByNamesMap = new TreeMap<>();
    private final List<NoteList> boards = new ArrayList<>();
    private List<String> tabs = new ArrayList<>();
    private List<Note> noteLists = new LinkedList<Note>();
    private TasksProvider trelloProvider;
    private TasksProvider todoistProvider;
    private TasksProvider chosenProvider;
    private String chosenListID;
    private NoteList chosenBoard;

    private transient final String trelloAppKey = "074f718830c8c5855fadfc28c2c5ffd6";
    private transient final String trelloAppSecret = "fa80fe8edab1f9a9e0d7030b15ed216c4d6ec174dd8e6efb9fd6a496b6d663b3";
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

        showTabsNav();

        checkProviders();

    }

    private void checkProviders() {
        if (trelloProvider == null && todoistProvider == null) {
            final ListView lv = (ListView) findViewById(R.id.tasksListView);
            final String[] values = {"Press login to get your lists"};
            final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, values);
            lv.setAdapter(adapter);
        } else
        {
            update();
        }
    }

    private void setupActionBar(ActionBar ab) {
        // set defaults for logo & home up
        boolean showHomeUp = false;
        ab.setDisplayHomeAsUpEnabled(showHomeUp);
        boolean useLogo = false;
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
        final EditText editTextName = (EditText) (dialogView.findViewById(R.id.noteName));
        final EditText editText = (EditText) (dialogView.findViewById(R.id.noteText));

        String textName = editTextName.getText() == null ? "" : editTextName.getText().toString();
        String text = editText.getText() == null ? "" : editText.getText().toString();

        if (chosenListID == null || textName.equals("")) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("ERROR")
                    .setMessage("You need to choose list and write note name")
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                }
                            }).create().show();
        } else {
            final String listID = chosenListID;
            new AddNoteAsyncTask(MainActivity.this, chosenProvider).execute(textName, text, listID);

            update();
        }
    }

    private void update() {
        try {
            noteLists = new LinkedList<>();
            boards.clear();
            String result = "";
            if (trelloProvider != null) {
                result = new ConnectionAsyncTask(this, trelloProvider).execute().get();
            }
            if (todoistProvider != null) {
                result = new ConnectionAsyncTask(this, todoistProvider).execute().get();
            }
            //return result != null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            //return false;
        } catch (ExecutionException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this,
                    "Authentication error", Toast.LENGTH_LONG).show();
            //return false;
        }
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
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                // switch to a progress animation
                if (isOnline()) {
                    item.setActionView(R.layout.indeterminate_progress_action);
                    String value = "nope";
                    try {
                        noteLists = new LinkedList<>();
                        boards.clear();

                        if (trelloProvider != null) {
                            value = new ConnectionAsyncTask(this, trelloProvider).execute().get();
                        }
                        if (todoistProvider != null) {
                            value = new ConnectionAsyncTask(this, todoistProvider).execute().get();
                        }
                    } catch (InterruptedException e) {
                        Log.e(TAG, "InterruptedException at onOptionsItemSelected", e);
                        return false;
                    } catch (ExecutionException e) {
                        Log.e(TAG, "ExecutionException at onOptionsItemSelected", e);
                        return false;
                    }
                    if (!value.equals("nope")) {
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
            case R.id.menu_login_trello: {
                loginTrello();
                return true;
            }
            case R.id.menu_login_todoist: {
                loginTodoist();
                return true;
            }
            case R.id.menu_choose_tabs: {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                        MainActivity.this);
                builderSingle.setTitle("Select tabs:");
                builderSingle.setNegativeButton("Done",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                refreshTabs();
                            }
                        });
                final CharSequence[] allTabs = getAllTabs();
                builderSingle.setMultiChoiceItems(allTabs, new boolean[allTabs.length],
                        new DialogInterface.OnMultiChoiceClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean selected) {
                                String strName = allTabs[which].toString();
                                if (selected) {
                                    tabs.add(strName);
                                } else {
                                    tabs.remove(strName);
                                }
                            }
                        });
                builderSingle.show();
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loginTrello() {
        String trelloToken = trelloOauth();
        trelloProvider = new TrelloProvider("074f718830c8c5855fadfc28c2c5ffd6", trelloToken);//"c74be1bc4cc64e0eb21aa8cd68067c11/////1cebce0d98eb0fc5a8fda7fecd5725aa500bcdb35edf7915d46453b8c7d38f3a");
        update();
    }

    private void loginTodoist() {
        final Dialog login = new Dialog(this);
        login.setContentView(R.layout.login_dialog);
        login.setTitle("Login to Todoist");

        Button btnLogin = (Button) login.findViewById(R.id.btnLogin);
        Button btnCancel = (Button) login.findViewById(R.id.btnCancel);
        final EditText txtUsername = (EditText)login.findViewById(R.id.txtUsername);
        final EditText txtPassword = (EditText)login.findViewById(R.id.txtPassword);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtUsername.getText().toString().trim().length() > 0 && txtPassword.getText().toString().trim().length() > 0)
                {
                    todoistProvider = new TodoistProvider(txtUsername.getText().toString(), txtPassword.getText().toString()); //("jakublasisz@gmail.com", "iamalazybastard");
                    update();
                    Toast.makeText(MainActivity.this,
                            "Login Sucessfull", Toast.LENGTH_LONG).show();
                    login.dismiss();
                }
                else
                {
                    Toast.makeText(MainActivity.this,
                            "Please enter Username and Password", Toast.LENGTH_LONG).show();
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login.dismiss();
            }
        });
        login.show();
    }


    private String trelloOauth() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://trello.com/1/authorize?key=074f718830c8c5855fadfc28c2c5ffd6&name=My+Application&expiration=1day&response_type=token&scope=read,write"));
        startActivity(browserIntent);

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    // Checks to see if the clip item contains an Intent, by testing to see if getIntent() returns null
        String pasteIntent = clipboard.getPrimaryClip().getItemAt(0).getText().toString();

        while (pasteIntent == null || pasteIntent.isEmpty() || pasteIntent.length() != 64)
            pasteIntent = clipboard.getPrimaryClip().getItemAt(0).getText().toString();

        return pasteIntent;
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
        performActionOnNote(name, info);
    }

    public void performActionOnNote(String name, AdapterView.AdapterContextMenuInfo info) {
        if (name.equals("Delete Note")) {
            String cardId = chosenBoard.get(info.position).getId();
            new RemoveNoteAsyncTask(MainActivity.this, chosenBoard.getProvider()).execute(cardId);
            update();
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
                            String textName = editTextName.getText() == null ? "" : editTextName.getText().toString();
                            String text = editText.getText() == null ? "" : editText.getText().toString();

                            if (textName.equals("")) {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("ERROR")
                                        .setMessage("You need to write note name")
                                        .setCancelable(false)
                                        .setPositiveButton("OK",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog,
                                                                        int whichButton) {
                                                    }
                                                }).create().show();
                            } else {
                                new EditNoteAsyncTask(MainActivity.this, chosenBoard.getProvider()).execute(cardId, textName, text);
                                update();                           }
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
        final int size = tabs.size();
        ab.removeAllTabs();
        for (int i = 0; i < size; i++) {
            ab.addTab(ab.newTab().setText(tabs.get(i)).setTabListener(this));
        }
    }

    private CharSequence[] getAllTabs() {
        CharSequence[] result = new CharSequence[boards.size()];
        for (int i = 0; i < boards.size(); i++) {
            result[i] = boards.get(i).getName();
        }
        return result;
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