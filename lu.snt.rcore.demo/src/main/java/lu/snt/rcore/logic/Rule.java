package lu.snt.rcore.logic;

/**
 * Created with IntelliJ IDEA.
 * User: assaad.moawad
 * Date: 15/05/13
 * Time: 14:32
 * To change this template use File | Settings | File Templates.
 */

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class Rule {
    private Literal head;
    private Collection body;      // of Literals (temporarily a LinkedList)
    private String name;
    private String mappingType;


  /* By calling this method, an always true local rule
     is created - body will be "null" */

    public Rule(Literal head, String name, String mappingType) {
        this.head = head;
        this.name = new String(name);
        this.mappingType = new String(mappingType);
        this.body = null;
    }

    public Rule(Literal head, String name, String mappingType, Collection literals) {
        this.head = head;
        this.name = new String(name);
        this.mappingType = new String(mappingType);
        this.body = new LinkedList(literals);
    }

    public Iterator getBody() {
        return ((this.body == null) ? null : this.body.iterator());
    }

    public String getRuleName() {
        return (new String(this.name));
    }

    public String getHeadName() {
        return this.head.getSignWithName();
    }

    public Literal getHead() {
        return (this.head);
    }

    public String getMappingType() {
        return (new String(this.mappingType));
    }

    public String print() {
        String s="";

        if(body!=null)
        {
        Iterator iter = body.iterator();
        boolean temp=false;

        while (iter.hasNext()) {
             Literal l = (Literal) iter.next();
            s+= l.fullName()+", ";
            temp=true;
        }
        if(temp)
            s=s.substring(0,s.length()-2) ;
        }
        s+=" -> ";
        s+= head.fullName();
        return s;
    }

    //To implement compare Rules to each other.
}
