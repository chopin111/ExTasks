package pl.edu.agh.pp.extasks.framework;

import java.util.List;

public interface TasksProvider {

    List<NoteList> getNotes();
    void getNotesFromService();

}
