package pl.edu.agh.pp.extasks.framework.exception;

/**
 * Exception to be thrown in case any provider initialization problem occurs.
 *
 * @author Jakub Lasisz
 * @author Maciej Sipko
 */
public class InitializationException extends Exception {
    public InitializationException(Exception e) {
        super(e);
    }
}
