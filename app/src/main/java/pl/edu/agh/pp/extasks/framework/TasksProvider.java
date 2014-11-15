package pl.edu.agh.pp.extasks.framework;

import org.trello4j.model.Board;

import java.util.List;
import java.util.Map;

/**
 * Common interface for all different providers.
 *
 * @author Jakub Lasisz
 * @author Maciej Sipko
 */
public interface TasksProvider {
    /**
     * Authenticates with provider.
     */
    void authenticate();

    /**
     * Initializes provider
     */
    void initialize();

    /**
     * Returns all the notes as a list of note lists.
     *
     * @return list of notes from specific provider
     */
    List<Note> getNotes();

    /**
     * Connects to the service, recieves all notes for specific accounts, converts and stores the notes into common model Note and NoteList.
     */
    void getNotesFromService();

    /**
     * Adds a new note with specified title and text.
     *
     * @param title
     * @param text
     */
    void addNote(String title, String text, String list);

    void removeNote(String cardId);

    Map<Board, List<Note>> getBoards();

}
