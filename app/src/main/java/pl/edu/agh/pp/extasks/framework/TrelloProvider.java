package pl.edu.agh.pp.extasks.framework;

import android.nfc.Tag;
import android.util.Log;

import org.trello4j.Trello;
import org.trello4j.TrelloImpl;
import org.trello4j.model.Board;
import org.trello4j.model.Card;
import org.trello4j.model.Checklist;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of a provider for Trello communication
 *
 * @author Jakub Lasisz
 * @author Maciej Sipko
 */
public class TrelloProvider implements TasksProvider {
    /**
     * Wrapper of the trello REST API
     */
    private Trello trelloManager;
    /**
     * List of trello boards
     */
    private List<Board> boards = new LinkedList<Board>();
    private List<org.trello4j.model.List> lists;
    private Map<String, List<org.trello4j.model.List>> listsByBoards = new HashMap<String, List<org.trello4j.model.List>>();
    private Map<String, List<Card>> cardsByLists = new HashMap<String, List<Card>>();
    private Map<String, List<Checklist>> checklistByCard = new HashMap<String, List<Checklist>>();
    private Map<String, List<Checklist.CheckItem>> checkitemByChecklist = new HashMap<String, List<Checklist.CheckItem>>();
    private List<Note> noteList = new LinkedList<Note>();
    private Board VIBoard;
    /**
     * Key representing a user account
     */
    private String key;
    /**
     * Token corresponding to the key
     */
    private String token;

    public TrelloProvider(String key, String token) {
        this.key = key;
        this.token = token;
    }

    @Override
    public void initialize() {
        trelloManager = new TrelloImpl(key, token);
        lists = trelloManager.getListByBoard(trelloManager.getBoardsByMember(trelloManager.getMemberByToken(token).getId()).get(0).getId());
    }

    @Override
    public void getNotesFromService() {
        boards = trelloManager.getBoardsByMember(trelloManager.getMemberByToken(token).getId());
        filterClosedBoards();
        for (Board b : boards) {
            Log.d("TrelloProvider", b.getId());
            for (Card c : trelloManager.getCardsByBoard(b.getId())) {
                if (!c.isClosed()) {
                    Note noteList = new Note(c.getName(), c.getDesc(), "");
                    this.noteList.add(noteList);
                }
            }
        }
    }

    private void filterClosedBoards() {
        Iterator<Board> it = boards.iterator();
        while (it.hasNext()) {
            Board board = it.next();
            if (board.isClosed()) {
                it.remove();
            }
        }
    }

    @Override
    public void addNote(String title, String text) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("desc", text);
        trelloManager.createCard(trelloManager.getListByBoard(VIBoard.getId()).get(0).getId(), title, map);
    }

    @Override
    public List<Note> getNotes() {
        return noteList;
    }

}
