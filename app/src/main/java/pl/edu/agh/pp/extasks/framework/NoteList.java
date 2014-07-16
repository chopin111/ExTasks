package pl.edu.agh.pp.extasks.framework;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * List of notes model for Extasks application.
 *
 * @author Jakub Lasisz
 * @author Maciej Sipko
 */
public class NoteList {

    /**
     * Name of a single note list.
     */
    private String name;

    /**
     * List of notes.
     */
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

    /**
     * Appends the specified Note to the end of the note list.
     *
     * @param note Note object to be appended to the list of notes.
     * @return true if appending succeeds
     */
    public boolean add(Note note) {
        return notes.add(note);
    }

    /**
     * Returns true list of notes contains specified element
     *
     * @param o element whose presence in note list is to be tested
     * @return true if note lists contains the specified element
     */
    public boolean contains(Object o) {
        return notes.contains(o);
    }

    /**
     * Returns note at the specified position in note list.
     *
     * @param i index of the note to return
     * @return Note at the specified position in note list.
     * @throws java.lang.IndexOutOfBoundsException if the index is out of note list range.
     */
    public Note get(int i) {
        return notes.get(i);
    }

    /**
     * Returns the index of the first occurence of the specified note in the note list, or -1 if the list does not contain the note.
     *
     * @param o note to search for
     * @return the index of the first occurence of the specified note in the note list, or -1 if the list does not contain the note.
     */
    public int indexOf(Object o) {
        return notes.indexOf(o);
    }

    /**
     * Tests wheter the note list is empty.
     *
     * @return true if the note list is empty, false otherwise.
     */
    public boolean isEmpty() {
        return notes.isEmpty();
    }

    /**
     * Returns iterator for the note list.
     *
     * @return iterator for the note list.
     */
    public Iterator<Note> iterator() {
        return notes.iterator();
    }

    /**
     * Returns list iterator for the note list.
     *
     * @return list iterator for the note list.
     */
    public ListIterator<Note> listIterator() {
        return notes.listIterator();
    }

    /**
     * Removes note at the specified position in the note list. Shifts any subsequent elements to the left.
     *
     * @param i the index of the note to be removed
     * @return the note that was removed from the note list
     * @throws java.lang.ArrayIndexOutOfBoundsException if the index is out of note list range
     */
    public Note remove(int i) {
        return notes.remove(i);
    }

    /**
     * Removes the first occurence of the specified note from the note lit, if it is present. Otherwise the list is unchanged.
     *
     * @param o note to be removed from the note list, if present
     * @return true if the note list contained the specified note
     */
    public boolean remove(Object o) {
        return notes.remove(o);
    }

    /**
     * Replaces the note at the specified position in the note list with the specified note.
     *
     * @param i    index of the note to replace
     * @param note note to be stored at the specified position
     * @return the note previously at the specified position
     * @throws java.lang.ArrayIndexOutOfBoundsException if the index is out of note list range
     */
    public Note set(int i, Note note) {
        return notes.set(i, note);
    }

    /**
     * Returns the number of notes in the note list.
     *
     * @return the number of notes in the note list
     */
    public int size() {
        return notes.size();
    }

    /**
     * Returns an array containing all of the notes in the note list in proper sequence.
     *
     * @return an array containing all of the notes in the note list in proper sequence
     */
    public Object[] toArray() {
        return notes.toArray();
    }

}
