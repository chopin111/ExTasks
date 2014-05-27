package pl.edu.agh.pp.extasks.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;


public class MainActivity extends Activity {

    static final String TAG = MainActivity.class.getSimpleName();
    boolean clicked = false;
    ArrayAdapter<String> adapter;
    ListView listView;


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
            button.setText("TrelloConnection");
            clicked = false;
        }
        MyAsyncTask myTask = new MyAsyncTask();
        myTask.execute();

    }

}