package pl.edu.agh.pp.extasks.framework;

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
    private Map<String, Board> boardsByName = new HashMap<String, Board>();
    private List<org.trello4j.model.List> lists;
    private Map<String, org.trello4j.model.List> listByName = new HashMap<String, org.trello4j.model.List>();
    private Map<String, List<Card>> cardsByLists = new HashMap<String, List<Card>>();
    private Map<String, List<Checklist>> checklistByCard = new HashMap<String, List<Checklist>>();
    private Map<String, List<Checklist.CheckItem>> checkitemByChecklist = new HashMap<String, List<Checklist.CheckItem>>();
    private List<Note> noteList = new LinkedList<Note>();
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
    public void authenticate() {
//        trelloManager = new TrelloImpl(key);
        trelloManager = new TrelloImpl(key, token);
    }

    @Override
    public void initialize() {
        List<Board> boards = trelloManager.getBoardsByMember(trelloManager.getMemberByToken(token).getId());
        boards = filterClosedBoards(boards);
        for (Board b : boards) {
            boardsByName.put(b.getName(), b);
        }

    }

    @Override
    public void getNotesFromService() {
        for (Board b : boardsByName.values()) {
            Log.d("TrelloProvider", b.getId());
            for (Card c : trelloManager.getCardsByBoard(b.getId())) {
                if (!c.isClosed()) {
                    Note noteList = new Note(c.getName(), c.getDesc(), "");
                    this.noteList.add(noteList);
                }
            }
        }
    }

    private List<Board> filterClosedBoards(List<Board> boardsList) {
        Iterator<Board> it = boardsList.iterator();
        while (it.hasNext()) {
            Board board = it.next();
            if (board.isClosed()) {
                it.remove();
            }
        }
        return boardsList;
    }

    @Override
    public void addNote(String title, String text, String list) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("desc", text);
        trelloManager.createCard(list, title, map);
    }

    @Override
    public List<Note> getNotes() {
        return noteList;
    }

}
