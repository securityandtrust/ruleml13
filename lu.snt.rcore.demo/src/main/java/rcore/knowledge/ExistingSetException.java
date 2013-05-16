package rcore.knowledge;

/**
 * Created with IntelliJ IDEA.
 * User: assaad.moawad
 * Date: 15/05/13
 * Time: 14:30
 * To change this template use File | Settings | File Templates.
 */
public class ExistingSetException
        extends Exception {
    public ExistingSetException() {
        super("ExistingSetException");
    }

    public ExistingSetException(String message) {
        super(message);
    }
}
