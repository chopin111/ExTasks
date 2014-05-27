package pl.edu.agh.pp.extasks.app;

import android.os.AsyncTask;
import android.util.Log;

import org.trello4j.Trello;
import org.trello4j.TrelloImpl;
import org.trello4j.model.Action;
import org.trello4j.model.Board;
import org.trello4j.model.Card;
import org.trello4j.model.Checklist;
import org.trello4j.model.List;

/**
 * Created by Kuba on 2014-05-25.
 */
public class MyAsyncTask extends AsyncTask<String, String, String> {
    @Override
    protected String doInBackground(String... strings) {
        Trello trello = new TrelloImpl("c74be1bc4cc64e0eb21aa8cd68067c11", "1cebce0d98eb0fc5a8fda7fecd5725aa500bcdb35edf7915d46453b8c7d38f3a");
        Board board = trello.getBoard("533ed8229e6d028403feaac4");
        java.util.List<Card> list = trello.getCardsByBoard("533ed8229e6d028403feaac4");
        java.util.List<List> llist = trello.getListByBoard("533ed8229e6d028403feaac4");
        Log.d(MainActivity.TAG, "boardName");
        Log.d(MainActivity.TAG, board.getName());
        Log.d(MainActivity.TAG, "cardNames");
        for (Card c : list) {
            Log.d(MainActivity.TAG, c.getName());
        }
        Card card = list.get(0);
        Log.d(MainActivity.TAG, "listNames");
        for (List l : llist) {
            Log.d(MainActivity.TAG, l.getName());
        }
        List first = llist.get(0);
        java.util.List<Card> clist = trello.getCardsByList(first.getId());
        Log.d(MainActivity.TAG, "cardNames");
        for (Card c : clist) {
            Log.d(MainActivity.TAG, c.getName());
        }
        java.util.List<Checklist> cllist = trello.getChecklistByBoard("533ed8229e6d028403feaac4");
        for (Checklist cl : cllist) {
            String name = cl.getName();
            Log.d(MainActivity.TAG, name);
            for (Checklist.CheckItem item : cl.getCheckItems()) {
                Log.d(MainActivity.TAG, item.getName());
            }
        }
        return "";
    }
}