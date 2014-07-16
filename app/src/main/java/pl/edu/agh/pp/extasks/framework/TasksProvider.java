package pl.edu.agh.pp.extasks.framework;

import java.util.List;

/**
 * Common interface for all different providers.
 *
 * @author Jakub Lasisz
 * @author Maciej Sipko
 */
public interface TasksProvider {
    /**
     * Returns all the notes as a list of note lists.
     *
     * @return list of notes from specific provider
     */
    List<NoteList> getNotes();

    /**
     * Connects to the service, recieves all notes for specific accounts, converts and stores the notes into common model Note and NoteList.
     */
    void getNotesFromService();

}
