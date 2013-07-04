package lu.snt.rcore.knowledge;

/**
 * Created with IntelliJ IDEA.
 * User: assaad.moawad
 * Date: 15/05/13
 * Time: 14:29
 * To change this template use File | Settings | File Templates.
 */

import lu.snt.rcore.logic.Literal;
import lu.snt.rcore.logic.Rule;

import java.util.Collection;


public interface KnowledgeBaseInterface {

    public void addRule(String setName, Rule rule)
            throws Throwable;

    public Collection getSignedRulesByHeadLiteral(String setName, Literal l)
            throws Throwable;
    public Collection getAllRulesByHeadLiteral(String setName, Literal l)
            throws Throwable;
}
