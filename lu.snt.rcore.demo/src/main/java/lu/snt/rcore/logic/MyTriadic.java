package lu.snt.rcore.logic;

/**
 * Created with IntelliJ IDEA.
 * User: assaad.moawad
 * Date: 15/05/13
 * Time: 14:32
 * To change this template use File | Settings | File Templates.
 */

import java.io.Serializable;

public class MyTriadic
        implements Serializable, Answer {
    public static byte StrictAnswer = 1;
    public static byte WeakAnswer = 2;
    public static byte NoAnswer = 3;

    private Literal literal;
    private byte typeOfAnswer;

    public MyTriadic(Literal literal, byte typeOfAnswer) {
        this.literal = literal;
        this.typeOfAnswer = typeOfAnswer;
    }

    public Literal getLiteral() {
        return (this.literal);
    }

    public byte getAnswerType() {
        return (this.typeOfAnswer);
    }

    public String toString() {
        //return(literal.getSignWithName());

        switch (this.typeOfAnswer) {
            case 1:
                return ("(" + literal.getSignWithName() + ":strict)");
            case 2:
                return ("(" + literal.getSignWithName() + ":weak)");
            case 3:
                return ("(" + literal.getSignWithName() + ":no)");
            default:
                return ("");
        }
    }
}