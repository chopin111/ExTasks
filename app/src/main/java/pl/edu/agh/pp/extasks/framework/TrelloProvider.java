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

public class TrelloProvider implements TasksProvider {

    private Trello trelloManager;
    private List<Board> boards = new LinkedList<Board>();
    private Map<String, List<org.trello4j.model.List>> listsByBoards = new HashMap<String, List<org.trello4j.model.List>>();
    private Map<String, List<Card>> cardsByLists = new HashMap<String, List<Card>>();
    private Map<String, List<Checklist>> checklistByCard = new HashMap<String, List<Checklist>>();
    private Map<String, List<Checklist.CheckItem>> checkitemByChecklist = new HashMap<String, List<Checklist.CheckItem>>();
    private List<NoteList> noteLists = new LinkedList<NoteList>();
    private final String key;
    private final String token;

    public TrelloProvider(String key, String token) {
        this.key = key;
        this.token = token;
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
