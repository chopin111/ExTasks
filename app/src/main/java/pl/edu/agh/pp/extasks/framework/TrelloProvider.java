package pl.edu.agh.pp.extasks.framework;

import org.trello4j.Trello;
import org.trello4j.TrelloImpl;
import org.trello4j.model.Board;
import org.trello4j.model.Card;
import org.trello4j.model.Checklist;

import java.util.HashMap;
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
    private Map<String, List<org.trello4j.model.List>> listsByBoards = new HashMap<String, List<org.trello4j.model.List>>();
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
    public void initialize() {
        trelloManager = new TrelloImpl(key, token);
    }

    @Override
    public void getNotesFromService() {
        boards = trelloManager.getBoardsByMember(trelloManager.getMemberByToken(token).getId());
        for (Board b : boards) {
            if (b.isClosed()) {
                continue;
            }
            for (Card c : trelloManager.getCardsByBoard(b.getId())) {
                Note noteList = new Note(c.getName(), c.getDesc(), "");
                this.noteList.add(noteList);
            }
        }
    }

    @Override
    public List<Note> getNotes() {
        return noteList;
    }

}
