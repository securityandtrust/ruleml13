package rcore.logic;

/**
 * Created with IntelliJ IDEA.
 * User: assaad.moawad
 * Date: 15/05/13
 * Time: 14:32
 * To change this template use File | Settings | File Templates.
 */

import java.io.Serializable;

public class Literal
        implements Serializable {
    private String name;
    private String location;
    private boolean sign;

    public static boolean getSign(String s) {
        s = s.trim();
        if (!s.startsWith("~"))
            return (true);
        else
            return (false);
    }

    public Literal(String name, String location, boolean sign) {
        this.name = new String(name);
        this.sign = sign;
        this.location = new String(location);
    }

    public String getName() {
        return (new String(this.name));
    }

    public String getLocation() {
        return (new String(this.location));
    }

    public String getSignWithName() {
        if (this.sign)
            return (new String(this.name));
        else
            return (new String("~" + this.name));
    }

    public boolean getSign() {
        return (this.sign);
    }

    public void reverseSign() {
        this.sign = !(this.sign);
    }

    public String toString() {
        return (new String(name));
    }
}


