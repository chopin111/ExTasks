package pl.edu.agh.pp.extasks.framework;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import nl.rgonline.lib.todoist.Item;
import nl.rgonline.lib.todoist.Project;
import nl.rgonline.lib.todoist.TodoistApi;
import nl.rgonline.lib.todoist.TodoistData;
import nl.rgonline.lib.todoist.TodoistException;

/**
 * Implementation of a provider for Todoist communication
 *
 * @author Jakub Lasisz
 * @author Maciej Sipko
 */
public class TodoistProvider implements TasksProvider {
    private TodoistApi todoistManager = new TodoistApi();
    private TodoistData data;
    private String login;
    private String password;
    private List<Note> noteList = new LinkedList<Note>();
    private List<NoteList> boards = new ArrayList<>();
    private Map<String, NoteList> listsByName = new TreeMap<>();

    public TodoistProvider(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public void authenticate() {
        todoistManager.login(login, password);
    }

    @Override
    public void initialize() throws InitializationException {
        try {
            todoistManager.syncAndGetUpdated();
            data = todoistManager.get();
        } catch (TodoistException e) {
            throw new InitializationException(e);
        }
    }

    @Override
    public List<Note> getNotes() {
        return noteList;
    }

    @Override
    public void getNotesFromService() {
        for (Project p : data.getProjects()) {
            if (p.isDeleted() || p.isIs_archived()) {
                continue;
            }
            NoteList board = new NoteList(p.getName(), String.valueOf(p.getId()), this);
            for (Item i : p.getItems()) {
                if (i.isChecked() || i.isArchived() || i.isDeleted()) {
                    continue;
                }
                final Note note = new Note(i.getContent(), "", "", i.getContent());
                this.noteList.add(note);
                board.add(note);
            }
            listsByName.put(p.getName(), board);
            boards.add(board);
        }
    }

    @Override
    public void addNote(String title, String text, String list) throws SynchronizationException {
        final Project project = data.getProjectById(Integer.parseInt(list));
        data.addItem(title, project);
        try {
            todoistManager.syncAndGetUpdated();
        } catch (TodoistException e) {
            throw new SynchronizationException(e);
        }
    }

    @Override
    public void removeNote(String cardId) throws SynchronizationException {
        final Item toRemoveNote = data.getItem(cardId);
        if (toRemoveNote != null) {
            toRemoveNote.setChecked(true);
        }
        try {
            todoistManager.syncAndGetUpdated();
        } catch (TodoistException e) {
            throw new SynchronizationException(e);
        }
    }

    @Override
    public List<NoteList> getBoards() {
        return boards;
    }

    @Override
    public Map<String, NoteList> getListsMap() {
        return listsByName;
    }

    @Override
    public void editNote(String cardId, String cardTitle, String cardText) throws SynchronizationException {
        final Item toEditNote = data.getItem(cardId);
        if (toEditNote == null) {
            return;
        }
        toEditNote.setContent(cardTitle);
        try {
            todoistManager.syncAndGetUpdated();
        } catch (TodoistException e) {
            throw new SynchronizationException(e);
        }
    }
}
