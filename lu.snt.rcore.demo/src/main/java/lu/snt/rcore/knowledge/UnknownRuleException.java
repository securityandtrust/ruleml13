package lu.snt.rcore.knowledge;

/**
 * Created with IntelliJ IDEA.
 * User: assaad.moawad
 * Date: 15/05/13
 * Time: 14:29
 * To change this template use File | Settings | File Templates.
 */
public class UnknownRuleException
        extends Exception {
    public UnknownRuleException() {
        super("UnknownRuleException");
    }

    public UnknownRuleException(String message) {
        super(message);
    }
}
