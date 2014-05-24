package pl.edu.agh.pp.extasks.app;

import com.google.api.services.tasks.model.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
* Asynchronously load the tasks.
*
* @author Yaniv Inbar
*/
class MyAsyncTask extends CommonAsyncTask {

    List<String> result = new ArrayList<String>();

    MyAsyncTask(MainActivity tasksSample) {
        super(tasksSample);
    }

    @Override
    protected void doInBackground() throws IOException {
        //List<String> result = new ArrayList<String>();
        List<Task> tasks =
                client.tasks().list("@default").setFields("items/title").execute().getItems();
        if (tasks != null) {
            for (Task task : tasks) {
                result.add(task.getTitle());
            }
        } else {
            result.add("No tasks.");
        }
        activity.tasksList = result;
    }

    static void run(MainActivity tasksSample) {
        new MyAsyncTask(tasksSample).execute();
    }
}
