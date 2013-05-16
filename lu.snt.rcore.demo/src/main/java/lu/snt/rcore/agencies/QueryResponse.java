package lu.snt.rcore.agencies;

/**
 * Created with IntelliJ IDEA.
 * User: assaad.moawad
 * Date: 15/05/13
 * Time: 14:24
 * To change this template use File | Settings | File Templates.
 */

import lu.snt.rcore.logic.Answer;
import lu.snt.rcore.logic.MyBoolean;

import java.io.Serializable;
import java.util.Collection;

public class QueryResponse implements Serializable {
    private Query question;

    private Answer returnValue;
    private Collection mappingSet;
    private Collection supportingSet;
    private Collection conflictingSet;

    public QueryResponse(Query question, Answer b, Collection c, Collection d) {
        this.question = question;
        this.returnValue = b;
        this.supportingSet = c;
        this.conflictingSet = d;
    }

    public Query getQuery() {
        return this.question;
    }

    public boolean isBooleanAnswer() {
        return (this.returnValue instanceof MyBoolean);
    }

    public boolean getReturnValue() {
        return (((MyBoolean) this.returnValue).getMyBoolean());
    }

    public Answer getReturnAnswer() {
        return (this.returnValue);
    }

    public Collection getSupportingSet() {
        return (this.supportingSet);
    }

    public Collection getConflictingSet() {
        return (this.conflictingSet);
    }
}
