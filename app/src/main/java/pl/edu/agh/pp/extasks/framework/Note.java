package pl.edu.agh.pp.extasks.framework;

public class Note {

    private String text;
    private String date;

    public Note(String text, String date) {
        this.text = text;
        this.date = date;
    }


    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }

}
