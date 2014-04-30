package pl.edu.agh.pp.extasks.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.ex_tasks.app.R;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    boolean clicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void buttonGTasksConOnclick(View v) {
        Button button = (Button) v;
        if (!clicked) {
            button.setText("klikłeś ziom!");
            clicked = true;
        } else {
            button.setText("GTasksConnection");
            clicked = false;
        }
        AccountManager accountManager = AccountManager.get(v.getContext());
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Log.d(TAG, accounts[0].name);

        //Log.d(TAG, intent.getDataString());

    }
}
