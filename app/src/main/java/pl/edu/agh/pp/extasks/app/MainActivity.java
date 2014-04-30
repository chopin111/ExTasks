package pl.edu.agh.pp.extasks.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.api.client.auth.oauth2.draft10.AccessProtectedResource;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

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
        if (id == R.id.action_settings || super.onOptionsItemSelected(item)) return true;
        else return false;
    }


    public void buttonGTasksConOnclick(View v) {
        Button button = (Button) v;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (!clicked) {
            button.setText("klikłeś ziom!");
            clicked = true;
        } else {
            button.setText("GTasksConnection");
            clicked = false;
        }
        AccountManager accountManager = AccountManager.get(v.getContext());
        Account[] accounts = accountManager.getAccountsByType("com.google"); /*TODO no account situation */
        Log.d(TAG, accounts[0].name);
        Account account = accounts[0];
        String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/tasks";

        accountManager.getAuthToken(account, AUTH_TOKEN_TYPE, null, v.getContext(), new AccountManagerCallback<Bundle>() {
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    // If the user has authorized your application to use the tasks API
                    // a token is available.
                    String token = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                    // Now you can use the Tasks API...
                    useTasksAPI(token);
                } catch (OperationCanceledException e) {
                    // TODO: The user has denied you access to the API, you should handle that
                } catch (Exception e) {
                }
            }
        }, null);
        //Log.d(TAG, intent.getDataString());

    }

    private void useTasksAPI(String accessToken) {
        // Setting up the Tasks API Service
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        AccessProtectedResource accessProtectedResource = new GoogleAccessProtectedResource(accessToken);
        Tasks service = new Tasks(transport, accessProtectedResource, new JacksonFactory());
        service.accessKey = "AIzaSyAN01Uou4oWpt6EB57Vr4eof4WbDZ3RJs4";
        service.setApplicationName("Google-TasksSample/1.0");

        // TODO: now use the service to query the Tasks API
    }

}
