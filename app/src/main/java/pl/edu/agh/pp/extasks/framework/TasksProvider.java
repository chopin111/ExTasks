package pl.edu.agh.pp.extasks.framework;

import java.util.List;
import java.util.Map;

import pl.edu.agh.pp.extasks.framework.exception.InitializationException;
import pl.edu.agh.pp.extasks.framework.exception.SynchronizationException;
import pl.edu.agh.pp.extasks.framework.model.Note;
import pl.edu.agh.pp.extasks.framework.model.NoteList;

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
    void initialize() throws InitializationException;

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
    void addNote(String title, String text, String list) throws SynchronizationException;

    /**
     * Removes note with specified id.
     *
     * @param cardId
     */
    void removeNote(String cardId) throws SynchronizationException;

    /**
     * Returns all NoteList objects as a list.
     *
     * @return list of NoteList objects related to provider
     */
    List<NoteList> getBoards();

    /**
     * Returns a Map with relation id of list -> NoteList object
     *
     * @return map of list ids to NoteList objects
     */
    Map<String, NoteList> getListsMap();

    /**
     * Edits specified note
     * @param cardId ID of note to be edited
     * @param cardTitle new title of note
     * @param cardText new text of note
     */
    void editNote(String cardId, String cardTitle, String cardText) throws SynchronizationException;

}
