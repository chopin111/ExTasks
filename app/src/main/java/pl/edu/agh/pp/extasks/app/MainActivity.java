package pl.edu.agh.pp.extasks.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;

import java.util.Collections;
import java.util.List;


public class MainActivity extends Activity {

    static final int REQUEST_AUTHORIZATION = 1;
    static final String TAG = MainActivity.class.getSimpleName();
    boolean clicked = false;
    private static String PREF_ACCOUNT_NAME;
    List<String> tasksList;
    Tasks service;
    int numAsyncTasks;
    ArrayAdapter<String> adapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "dupa");
        setContentView(R.layout.activity_main);
    }

    void refreshView() {
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tasksList);
        listView.setAdapter(adapter);
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

        /*GoogleAsyncTask task = new GoogleAsyncTask();
        task.execute();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = task.getResult();
        Log.d(TAG, result);*/

       /* HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        String clientId = "129983892196-ahdem6mt5pau5sufj9oc1rocngqmr4e6.apps.googleusercontent.com";
        String clientSecret = "AIzaSyAN01Uou4oWpt6EB57Vr4eof4WbDZ3RJs4";


        String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";
        String scope = "https://www.googleapis.com/auth/tasks";

// Step 1: Authorize -->
        String authorizationUrl = new GoogleAuthorizationRequestUrl(clientId, redirectUrl, scope)
                .build();

        // Point or redirect your user to the authorizationUrl.
        System.out.println("Go to the following link in your browser:");
        System.out.println(authorizationUrl);

        // Read the authorization code from the standard input stream.
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("What is the authorization code?");
        try {
            String code = in.readLine();
            // End of Step 1 <--

            // Step 2: Exchange -->
            AccessTokenResponse response = new GoogleAuthorizationCodeGrant(httpTransport, jsonFactory,
                    clientId, clientSecret, code, redirectUrl).execute();
            // End of Step 2 <--

            GoogleAccessProtectedResource accessProtectedResource = new GoogleAccessProtectedResource(
                    response.accessToken, httpTransport, jsonFactory, clientId, clientSecret,
                    response.refreshToken);

            Tasks service = new Tasks(httpTransport, jsonFactory, accessProtectedResource);
        } catch (IOException e) {
            e.printStackTrace();
        }*/



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
        Account[] accounts = accountManager.getAccountsByType("com.google"); //*//*TODO no account situation *//*
        Log.d(TAG, accounts[0].name);
        Account account = accounts[0];
        PREF_ACCOUNT_NAME = account.name;
        String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/tasks";

        accountManager.getAuthToken(account, AUTH_TOKEN_TYPE, null, this, new AccountManagerCallback<Bundle>() {
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    // If the user has authorized your application to use the tasks API
                    // a token is available.
                    String token = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                    // Now you can use the Tasks API...
                    useTasksAPI(token);
                } /*catch (OperationCanceledException e) {
                    // TODO: The user has denied you access to the API, you should handle that
                }*/ catch (Exception e) {
                }
            }
        }, null);
        //Log.d(TAG, intent.getDataString());

    }

    private void useTasksAPI(String accessToken) {
        // Setting up the Tasks API Service
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
//        AccessProtectedResource accessProtectedResource = new GoogleAccessProtectedResource(accessToken);
        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(this, Collections.singleton(TasksScopes.TASKS));
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        //credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
        credential.setSelectedAccountName("magicmacko@gmail.com");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, accessToken);

        Log.d(TAG, "service");
        service = new Tasks.Builder(transport, new GsonFactory(), credential).setApplicationName("Google-TasksSample/1.0").build();
        //Tasks service = new Tasks.Builder(transport, new GsonFactory(), accessProtectedResource).setApplicationName("Google-TasksSample/1.0").build();
        //service.accessKey = "AIzaSyAN01Uou4oWpt6EB57Vr4eof4WbDZ3RJs4";
        MyAsyncTask task = new MyAsyncTask(this);
        task.execute();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (String s : task.result) {
            Log.d(TAG, s);
        }
        /*List<String> result = null;
        List<Task> tasks = null;
        try {
            result = new ArrayList<String>();
            tasks =
                    service.tasks().list("@default").execute().getItems();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (tasks != null) {
            for (Task t : tasks) {
                result.add(t.getTitle());
            }
        } else {
            result.add("No tasks.");
        }
        for (String res : result) {
            Log.d(TAG, res);
        }

//    try {
//            service.tasks().list("@default");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        Log.d(TAG, "getItems");
//        try {
//            List taskLists = service.tasklists().list().execute().getItems();
//            Log.d(TAG, "print");
//            for (Object taskList : taskLists) {
//                Log.d(TAG, taskList.toString());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        Log.d(TAG, "new");
        Task t = new Task();
        t.setTitle("new task");
        t.setNotes("dupa");
        try

        {
            service.tasks().insert("@default", t).execute();
        } catch (
                Exception e
                )

        {
            Log.d(TAG, "dupa");
        }*/
        // TODO: now use the service to query the Tasks API
    }

}
