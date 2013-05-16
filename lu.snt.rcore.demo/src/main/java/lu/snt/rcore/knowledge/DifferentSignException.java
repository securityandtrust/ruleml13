package lu.snt.rcore.knowledge;

/**
 * Created with IntelliJ IDEA.
 * User: assaad.moawad
 * Date: 15/05/13
 * Time: 14:30
 * To change this template use File | Settings | File Templates.
 */
public class DifferentSignException
        extends Exception {
    public DifferentSignException() {
        super("DifferentSignException: A rule with the opposite sign exists");
    }

    public DifferentSignException(String message) {
        super(message);
    }
}