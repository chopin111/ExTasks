package pl.edu.agh.pp.extasks.framework;

import android.util.Log;


import org.trello4j.Trello;
import org.trello4j.TrelloImpl;
import org.trello4j.model.Board;
import org.trello4j.model.Card;
import org.trello4j.model.Checklist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import nl.rgonline.lib.todoist.Item;
import nl.rgonline.lib.todoist.Label;
import nl.rgonline.lib.todoist.Project;
import nl.rgonline.lib.todoist.TodoistApi;
import nl.rgonline.lib.todoist.TodoistData;
import nl.rgonline.lib.todoist.TodoistException;

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
    private Map<Board, List<Note>> boardsMap = new HashMap<>();
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
        for (Board b : boards) {
            if (b.isClosed()) {
                continue;
            }
            List<org.trello4j.model.List> listOfLists = trelloManager.getListByBoard(b.getId());
            List<Card> listTmp = trelloManager.getCardsByBoard(b.getId());
            List<Note> notesTmp = new ArrayList<>();
            for (Card card:listTmp) {
                notesTmp.add(new Note(card.getName(), card.getDesc(), "", card.getId()));
            }
            boardsMap.put(b, notesTmp);

            for (org.trello4j.model.List l : listOfLists) {
                listByName.put(b.getName() + "/" + l.getName(), l);
            }
            boardsByName.put(b.getName(), b);
        }

    }

    @Override
    public void getNotesFromService() {
        TodoistApi todoistApi = new TodoistApi();
        todoistApi.login("jakublasisz@gmail.com", "iamalazybastard");
        try {
            todoistApi.syncAndGetUpdated();
            TodoistData data = todoistApi.get();
            List<Item> items = data.getItems();
            for (Item i : items) {
                Log.d("item", i.getProject().toString() + " " + i.getContent() + " " + i.toString());
            }
            List<Label> labels = data.getLabels();
            for (Label l : labels) {
                Log.d("label", l.getName() + " " + l.toString());
            }
            for (Project p : data.getProjects()) {
                Log.d("project", p.getName() + " " + p.toString());
            }
            Log.d("todoistt", data.toString());

        } catch (Exception e) {
            Log.d("dupa", "dupa");
        }
        for (Board b : boardsByName.values()) {
            Log.d("TrelloProvider", b.getId());
            for (Card c : trelloManager.getCardsByBoard(b.getId())) {
                if (!c.isClosed()) {
                    Note noteList = new Note(c.getName(), c.getDesc(), "", c.getId());
                    this.noteList.add(noteList);
                }
            }
        }
    }

    @Override
    public void addNote(String title, String text, String list) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("desc", (Object)text);
        Card c = trelloManager.createCard(list, title, map);
        //trelloManager.deleteCard(c.getId());
    }

    @Override
    public void removeNote(String cardId) {
        trelloManager.deleteCard(cardId);
    }

    @Override
    public List<Note> getNotes() {
        return noteList;
    }

    public Map<String, org.trello4j.model.List> getLists() {
        return listByName;
    }

    public Map<Board, List<Note>> getBoards() {
        return boardsMap;
    }

}
