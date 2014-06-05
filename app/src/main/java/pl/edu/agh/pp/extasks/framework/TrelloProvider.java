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

/**Implementation of a provider for Trello communication
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
    /**
     * List of note lists on every board
     */
    private List<NoteList> noteLists = new LinkedList<NoteList>();
    /**
     * Key representing a user account
     */
    private String key;
    /**
     * Token corresponding to the key
     */
    private String token;

    public TrelloProvider(){}

    public TrelloProvider(String key, String token) {
        this.key = key;
        this.token = token;
        trelloManager = new TrelloImpl(key, token);
    }

    public void setProvider(Trello provider) {
        this.trelloManager = provider;
    }

    public void setToken(String newToken) {
        this.token = newToken;
    }

    public void setKey(String newKey) {
        this.key = newKey;
    }

    @Override
    public void getNotesFromService() {
        boards = trelloManager.getBoardsByMember(trelloManager.getMemberByToken(token).getId());
        for (Board b : boards) {
            if (b.isClosed()) {
                continue;
            }
            for (Card c : trelloManager.getCardsByBoard(b.getId())) {
                NoteList noteList = new NoteList(c.getName());
                for (Checklist cl : trelloManager.getChecklistByCard(c.getId())) {
                    for (Checklist.CheckItem ci : trelloManager.getCheckItemsByChecklist(cl.getId())) {
                        noteList.add(new Note(ci.getName(), ""));
                    }
                }
                noteLists.add(noteList);
            }
        }
    }

    @Override
    public List<NoteList> getNotes() {
        return noteLists;
    }

}
