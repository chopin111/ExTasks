package pl.edu.agh.pp.extasks.framework.exception;

/**
 * Exception to be thrown in case network synchronization with server problem occurs.
 *
 * @author Jakub Lasisz
 * @author Maciej Sipko
 */
public class SynchronizationException extends Exception {
    public SynchronizationException(Exception e) {
        super(e);
    }
}
