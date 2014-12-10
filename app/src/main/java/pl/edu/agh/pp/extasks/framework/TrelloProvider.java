package pl.edu.agh.pp.extasks.framework;

import org.trello4j.Trello;
import org.trello4j.TrelloImpl;
import org.trello4j.model.Board;
import org.trello4j.model.Card;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pl.edu.agh.pp.extasks.framework.exception.InitializationException;
import pl.edu.agh.pp.extasks.framework.model.Note;
import pl.edu.agh.pp.extasks.framework.model.NoteList;

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
    private List<NoteList> boards = new ArrayList<>();
    private Map<String, NoteList> listByName = new HashMap<String, NoteList>();
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
        trelloManager = new TrelloImpl(key, token);
    }

    @Override
    public void initialize() throws InitializationException {
        final List<Board> boards = trelloManager.getBoardsByMember(trelloManager.getMemberByToken(token).getId());
        clear();
        for (Board b : boards) {
            if (b.isClosed()) {
                continue;
            }
            final List<org.trello4j.model.List> listOfLists = trelloManager.getListByBoard(b.getId());
            final List<Card> currentBoardCards = trelloManager.getCardsByBoard(b.getId());
            final List<Note> currentBoardNotes = new ArrayList<>();
            final NoteList board = new NoteList(b.getName(), b.getId(), this);
            for (Card card : currentBoardCards) {
                board.add(new Note(card.getName(), card.getDesc(), "", card.getId()));
            }
            this.boards.add(board);
            for (org.trello4j.model.List l : listOfLists) {
                final NoteList list = new NoteList(l.getName(), l.getId(), this);
                listByName.put(b.getName() + "/" + l.getName(), list);
            }
            boardsByName.put(b.getName(), b);
        }
    }

    @Override
    public void getNotesFromService() {
        for (Board b : boardsByName.values()) {
            for (Card c : trelloManager.getCardsByBoard(b.getId())) {
                if (!c.isClosed()) {
                    final Note noteList = new Note(c.getName(), c.getDesc(), "", c.getId());
                    this.noteList.add(noteList);
                }
            }
        }
    }

    @Override
    public void addNote(String title, String text, String list) {
        final Map<String, Object> map = new HashMap<>();
        map.put("desc", text);
        trelloManager.createCard(list, title, map);
    }

    @Override
    public void removeNote(String cardId) {
        trelloManager.deleteCard(cardId);
    }

    @Override
    public List<Note> getNotes() {
        return noteList;
    }

    @Override
    public List<NoteList> getBoards() {
        return boards;
    }

    @Override
    public Map<String, NoteList> getListsMap() {
        return listByName;
    }

    /**
     * Edits specified note
     * @param cardId ID of note to be edited
     * @param cardTitle new title of note
     * @param cardText new text of note
     */
    public void editNote(String cardId, String cardTitle, String cardText) {
        trelloManager.updateCard(cardId, cardTitle, cardText);
    }

    /**
     * Clears all data in Trello provider
     */
    public void clear() {
        boards.clear();
        listByName.clear();
        boardsByName.clear();
        noteList.clear();
    }
}
