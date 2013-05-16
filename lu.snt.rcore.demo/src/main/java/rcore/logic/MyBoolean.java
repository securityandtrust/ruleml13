package rcore.logic;

/**
 * Created with IntelliJ IDEA.
 * User: assaad.moawad
 * Date: 15/05/13
 * Time: 14:32
 * To change this template use File | Settings | File Templates.
 */

import java.io.Serializable;

public class MyBoolean
        implements Serializable, Answer {
    private boolean b;

    public MyBoolean(boolean b) {
        this.b = b;
    }

    public boolean getMyBoolean() {
        return (this.b);
    }

    public void setMyBoolean(boolean b) {
        this.b = b;
    }

}