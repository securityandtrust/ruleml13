package rcore.knowledge;

/**
 * Created with IntelliJ IDEA.
 * User: assaad.moawad
 * Date: 15/05/13
 * Time: 14:31
 * To change this template use File | Settings | File Templates.
 */

import rcore.logic.Answer;

import java.util.Collection;

public class AnswerSet {
    private Answer answer = null;
    private Collection c = null;

    public AnswerSet(Answer answer, Collection c) {
        this.answer = answer;
        this.c = c;
    }

    public Collection getSet() {
        return (this.c);
    }

    public Answer getAnswer() {
        return (this.answer);
    }
}

