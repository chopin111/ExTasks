package pl.edu.agh.pp.extasks.framework;

/**
 * Note model for ExTasks application.
 *
 * @author Jakub Lasisz
 * @author Maciej Sipko
 *
 */
public class Note {
    /**
     * Contents of a single note.
     */
    private String text;
    /**
     * Creation date of a single note.
     */
    private String date;

    public Note(String text, String date) {
        this.text = text;
        this.date = date;
    }

    /**
     *
     * @return Note contents.
     */
    public String getText() {
        return text;
    }

    /**
     *
     * @return Note creation date.
     */
    public String getDate() {
        return date;
    }

}
