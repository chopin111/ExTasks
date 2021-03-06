package pl.edu.agh.pp.extasks.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
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
    private transient final String TRELLO_APP_KEY = "074f718830c8c5855fadfc28c2c5ffd6";
    private ConnectionDetector connectionDetector;
    private List<String> tabs = new ArrayList<>();
    private List<Note> noteLists = new LinkedList<Note>();
    private TasksProvider trelloProvider;
    private TasksProvider todoistProvider;
    private TasksProvider chosenProvider;
    private String chosenListID;
    private NoteList chosenBoard;
    private String trelloToken;

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
        connectionDetector = new ConnectionDetector(getApplicationContext());

    }

    /**
     * Checks if any of providers is logged in
     */
    private void checkProviders() {
        if (trelloProvider == null && todoistProvider == null) {
            final ListView lv = (ListView) findViewById(R.id.tasksListView);
            final String[] values = {"Press login to get your lists"};
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
            lv.setAdapter(adapter);
        } else {
            if (connectionDetector.isConnectingToInternet()) {
                update();
            } else {
                showAlertDialog(MainActivity.this, "No Internet Access", "You don't have access to Internet");
            }
        }
    }

    private void setupActionBar(ActionBar ab) {
        boolean showHomeUp = false;
        ab.setDisplayHomeAsUpEnabled(showHomeUp);
        boolean useLogo = false;
        ab.setDisplayUseLogoEnabled(useLogo);

        if (noteLists.size() == 0) {
            ab.addTab(ab.newTab().setText("Empty tab").setTabListener(this));
        }

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
            if (connectionDetector.isConnectingToInternet()) {
                new AddNoteAsyncTask(MainActivity.this, chosenProvider).execute(textName, text, listID);

                update();
            } else {
                showAlertDialog(MainActivity.this, "No Internet Access", "You don't have access to Internet");
            }
        }
    }

    /**
     * Performes update for logged in providers
     */
    private void update() {
        if (!connectionDetector.isConnectingToInternet()) {
            showAlertDialog(MainActivity.this, "No Internet Access", "You don't have access to Internet");
            return;
        }
        try {
            noteLists = new LinkedList<>();
            boards.clear();
            String result = "";
            if (trelloProvider != null) {
                result = new ConnectionAsyncTask(this, trelloProvider).execute().get();
                if (!result.equals("")) {
                    throw new ExecutionException(new Throwable(result));
                }
            }
            if (todoistProvider != null) {
                result = new ConnectionAsyncTask(this, todoistProvider).execute().get();
                if (!result.equals("")) {
                    throw new ExecutionException(new Throwable(result));
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this,
                    "Authentication error", Toast.LENGTH_LONG).show();
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

        final MenuItem refresh = menu.findItem(R.id.menu_refresh);
        refresh.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            // on selecting show progress spinner for 1s
            public boolean onMenuItemClick(MenuItem item) {
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
                if (!connectionDetector.isConnectingToInternet()) {
                    showAlertDialog(MainActivity.this, "No Internet Access", "You don't have access to Internet");
                    return false;
                }
                AddNoteDialogFragment dialog = new AddNoteDialogFragment();
                dialog.show(getSupportFragmentManager(), "Add Note Dialog");
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                if (connectionDetector.isConnectingToInternet()) {
                    item.setActionView(R.layout.indeterminate_progress_action);
                    update();
                    return true;
                } else {
                    showAlertDialog(MainActivity.this, "No Internet Access", "You don't have access to Internet");
                    return false;
                }
            case R.id.menu_login_trello: {
                if (connectionDetector.isConnectingToInternet()) {
                    loginTrello();
                    return true;
                }
                showAlertDialog(MainActivity.this, "No Internet Access", "You don't have access to Internet");
                return false;
            }
            case R.id.menu_login_todoist: {
                if (connectionDetector.isConnectingToInternet()) {
                    loginTodoist();
                    return true;
                }
                showAlertDialog(MainActivity.this, "No Internet Access", "You don't have access to Internet");
                return false;
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
                                refreshTextView();
                            }
                        });
                final CharSequence[] allTabs = getAllTabs();
                boolean[] selectedTabs = getSelectedTabs(allTabs);

                builderSingle.setMultiChoiceItems(allTabs, selectedTabs,
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

    /**
     * Returnes tabs which were selected from dialog
     * @param allTabs CharSequence array containing all tab names
     * @return boolean array telling which tabs were selected
     */
    private boolean[] getSelectedTabs(CharSequence[] allTabs) {
        boolean[] selectedTabs = new boolean[allTabs.length];
        for (int i = 0; i < allTabs.length; i++) {
            if (tabs.contains(allTabs[i])) {
                selectedTabs[i] = true;
            } else
                selectedTabs[i] = false;
        }
        return selectedTabs;
    }

    /**
     * Creates Trello login popup
     */
    private void loginTrello() {
        if (trelloProvider == null) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("INFO")
                    .setMessage("Please copy token from the browser.")
                    .setCancelable(true)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    dialog.dismiss();
                                    trelloToken = trelloOauth();
                                    trelloProvider = new TrelloProvider("074f718830c8c5855fadfc28c2c5ffd6", trelloToken);//"c74be1bc4cc64e0eb21aa8cd68067c11/////1cebce0d98eb0fc5a8fda7fecd5725aa500bcdb35edf7915d46453b8c7d38f3a");
                                    update();
                                }
                            }).create().show();
        }
        update();
    }

    /**
     * Todoist authorization - creates dialog for login and password
     */
    private void loginTodoist() {
        final Dialog login = new Dialog(this);
        login.setContentView(R.layout.login_dialog);
        login.setTitle("Login to Todoist");

        Button btnLogin = (Button) login.findViewById(R.id.btnLogin);
        Button btnCancel = (Button) login.findViewById(R.id.btnCancel);
        final EditText txtUsername = (EditText) login.findViewById(R.id.txtUsername);
        final EditText txtPassword = (EditText) login.findViewById(R.id.txtPassword);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtUsername.getText().toString().trim().length() > 0 && txtPassword.getText().toString().trim().length() > 0) {
                    todoistProvider = new TodoistProvider(txtUsername.getText().toString(), txtPassword.getText().toString()); //("jakublasisz@gmail.com", "iamalazybastard");
                    update();
                    Toast.makeText(MainActivity.this, "Login Successfull", Toast.LENGTH_LONG).show();
                    login.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "Please enter Username and Password", Toast.LENGTH_LONG).show();
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

    /**
     * Trello authorization for getting token
     * @return token obtained from browser
     */
    private String trelloOauth() {
        final String url = String.format("https://trello.com/1/authorize?key=%s&name=ExTasks&expiration=never&response_type=token&scope=read,write", TRELLO_APP_KEY);
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String pasteIntent = "";
        if (clipboard.getPrimaryClip() != null) {
            pasteIntent = clipboard.getPrimaryClip().getItemAt(0).getText().toString();
        }

        while (pasteIntent == null || pasteIntent.isEmpty() || pasteIntent.length() != 64) {
            if (clipboard.getPrimaryClip() != null) {
                pasteIntent = clipboard.getPrimaryClip().getItemAt(0).getText().toString();
            } else
                pasteIntent = "";
        }
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
        chosenBoard = boards.get(0);
        for (NoteList board : boards) {
            if (board.getName().equals(tabs.get(tabNo))) {
                chosenBoard = board;
                break;
            }
        }
        final List<Note> chosenBoardNotes = chosenBoard.getNotes();

        final List<String> values = createValues(chosenBoardNotes);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
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

    /**
     * Performes actions on long click on notes
     */
    public void performActionOnNote(String name, AdapterView.AdapterContextMenuInfo info) {
        if (name.equals("Delete Note")) {
            String cardId = chosenBoard.get(info.position).getId();
            new RemoveNoteAsyncTask(MainActivity.this, chosenBoard.getProvider()).execute(cardId);
            update();
            refreshTabs();
            refreshTextView();
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
                                update();
                                refreshTabs();
                                refreshTextView();
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
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

    /**
     * Gets all lists currently loaded from providers
     * @return CharSequence array containing all tab names
     */
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
    }

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    /**
     * Creates dialog to choose list
     */
    public void chooseList(View view) {
        if (trelloProvider != null) {
            listByNamesMap.putAll(trelloProvider.getListsMap());
        }
        if (todoistProvider != null) {
            listByNamesMap.putAll(todoistProvider.getListsMap());
        }
        final ChooseListDialog dialog = ChooseListDialog.newInstance(listByNamesMap.keySet().toArray(new String[listByNamesMap.size()]));
        dialog.appendItemsMap(listByNamesMap);
        dialog.show(getSupportFragmentManager(), "Choose dialog");
    }

    public void updateBoardsMap(List<NoteList> boards) {
        this.boards.addAll(boards);
    }

    /**
     * Creates list of notes titles for given list of Note objects
     * @param chosenBoardNotes list of Note objects
     * @return list of Strings containing notes titles
     */
    private List<String> createValues(List<Note> chosenBoardNotes) {
        List<String> values = new ArrayList<>(chosenBoardNotes.size());
        for (Note n : chosenBoardNotes) {
            values.add(n.getTitle());
        }
        return values;
    }

    /**
     * Displays alert dialog
     * @param context dialog context
     * @param title dialog title
     * @param message message content
     */
    public void showAlertDialog(Context context, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon(R.drawable.fail);
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.show();
    }

}