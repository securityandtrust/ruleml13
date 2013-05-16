package rcore.agencies;

/**
 * Created with IntelliJ IDEA.
 * User: assaad.moawad
 * Date: 15/05/13
 * Time: 14:24
 * To change this template use File | Settings | File Templates.
 */

import rcore.logic.Answer;
import rcore.logic.Literal;
import rcore.logic.MyBoolean;

import java.io.Serializable;
import java.util.Collection;


public class Query implements Serializable {
    private Literal query;
    private Answer localAnswer;


    private String nameowner;  //the component that created this query
    private String namedestination;
    private int idprocess;     // like the port processing the query in the Owner

    private Collection suppMappingsSet;
    private Collection conflMappingsSet;
    private Collection history;

    public Query(String owner, int idProcess, String destination, Literal query, boolean localAnswer, Collection ss, Collection cs, Collection history) {
        this.nameowner = owner;
        this.idprocess = idProcess;
        this.namedestination = destination;

        this.query = query;
        this.suppMappingsSet = ss;
        this.conflMappingsSet = cs;
        this.localAnswer = new MyBoolean(localAnswer);
        this.history = history;
    }

    public Query(String owner, String destination, int idProcess, Literal query, Answer localAnswer, Collection ss, Collection cs, Collection history) {
        this.nameowner = owner;
        this.idprocess = idProcess;
        this.namedestination = destination;

        this.query = query;
        this.suppMappingsSet = ss;
        this.conflMappingsSet = cs;
        this.localAnswer = localAnswer;
        this.history = history;
    }

    public String getOwner() {
        return nameowner;
    }

    public String getDestinator() {
        return namedestination;
    }

    public int getProcessId() {
        return idprocess;
    }

    public Literal getLiteral() {
        return (this.query);
    }

    public Answer getLocalAnswer() {
        return (this.localAnswer);
    }

    public Collection getSupportingMappings() {
        return (this.suppMappingsSet);
    }

    public Collection getConflictingMappings() {
        return (this.conflMappingsSet);
    }

    public Collection getHistory() {
        return (this.history);
    }

}
