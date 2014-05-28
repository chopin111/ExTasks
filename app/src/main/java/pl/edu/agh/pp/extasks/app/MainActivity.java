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

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends SherlockFragmentActivity implements ActionBar.TabListener {

    static final String TAG = MainActivity.class.getSimpleName();

    static ArrayList<String> listsNames = new ArrayList<String>();
    static ArrayList<ArrayList<String>> tasksLists = new ArrayList<ArrayList<String>>();

    private final Handler handler = new Handler();
    private boolean useLogo = false;
    private boolean showHomeUp = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final ActionBar ab = getSupportActionBar();

        // set defaults for logo & home up
        ab.setDisplayHomeAsUpEnabled(showHomeUp);
        ab.setDisplayUseLogoEnabled(useLogo);

        // set up tabs nav
        if (listsNames.size() == 0) {
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
        final MenuItem refresh = (MenuItem) menu.findItem(R.id.menu_refresh);
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
                    value = new MyAsyncTask().execute().get();
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
                }
                else
                    return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshTextView() {
        Log.d(TAG, "refreshTextView");
        TextView tv = (TextView) findViewById(R.id.tasksList);
        int whichNo = getActionBar().getSelectedTab().getPosition();
        boolean first = true;
        ArrayList<String> tasksList = tasksLists.get(whichNo);
        for (String task : tasksList) {
            if (first) {
                tv.setText(task + "\n");
                first = false;
            } else
                tv.append(task + "\n");
        }
    }

    private void refreshTabs() {
        ActionBar ab = getSupportActionBar();
        int size = listsNames.size();
        for (int i = 0; i < size; i++) {
            if (i < ab.getTabCount()) {
                ab.getTabAt(i).setText(listsNames.get(i)).setTabListener(this);
            } else
                ab.addTab(ab.newTab().setText(listsNames.get(i)).setTabListener(this));
        }
    }

    private void showTabsNav() {
        ActionBar ab = getSupportActionBar();
        if (ab.getNavigationMode() != ActionBar.NAVIGATION_MODE_TABS) {
            ab.setDisplayShowTitleEnabled(false);
            ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
    }

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        if (listsNames.size() > 0) {
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