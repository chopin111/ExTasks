package pl.edu.agh.pp.extasks.framework;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class NoteList {

    private String name;
    private List<Note> notes;

    public NoteList(String name) {
        this.name = name;
        notes = new LinkedList<Note>();
    }

    public String getName() {
        return name;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public boolean add(Note note) {
        return notes.add(note);
    }

    public boolean contains(Object o) {
        return notes.contains(o);
    }

    public Note get(int i) {
        return notes.get(i);
    }

    public int indexOf(Object o) {
        return notes.indexOf(o);
    }

    public boolean isEmpty() {
        return notes.isEmpty();
    }

    public Iterator<Note> iterator() {
        return notes.iterator();
    }

    public ListIterator<Note> listIterator() {
        return notes.listIterator();
    }

    public Note remove(int i) {
        return notes.remove(i);
    }

    public boolean remove(Object o) {
        return notes.remove(o);
    }

    public Note set(int i, Note note) {
        return notes.set(i, note);
    }

    public int size() {
        return notes.size();
    }

    public Object[] toArray() {
        return notes.toArray();
    }

}
