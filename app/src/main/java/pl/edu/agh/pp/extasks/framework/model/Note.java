package pl.edu.agh.pp.extasks.framework.model;

/**
 * Note model for ExTasks application.
 *
 * @author Jakub Lasisz
 * @author Maciej Sipko
 */
public class Note {
    /**
     * Title of a single note.
     */
    private String title;
    /**
     * Contents of a single note.
     */
    private String text;
    /**
     * Creation date of a single note.
     */
    private String date;

    private String id;

    public Note(String title, String text, String date, String id) {
        this.title = title;
        this.text = text;
        this.date = date;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    /**
     * @return Note contents.
     */
    public String getText() {
        return text;
    }

    /**
     * @return Note creation date.
     */
    public String getDate() {
        return date;
    }

    public String getId() {
        return id;
    }
}
