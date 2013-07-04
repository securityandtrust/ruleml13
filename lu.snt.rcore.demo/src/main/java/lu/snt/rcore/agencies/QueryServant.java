package lu.snt.rcore.agencies;

/**
 * Created with IntelliJ IDEA.
 * User: assaad.moawad
 * Date: 15/05/13
 * Time: 14:23
 * To change this template use File | Settings | File Templates.
 */

import lu.snt.rcore.QueryInterface;
import lu.snt.rcore.knowledge.AnswerSet;
import lu.snt.rcore.knowledge.KnowledgeBase;
import lu.snt.rcore.knowledge.UnknownRuleException;
import lu.snt.rcore.logic.Literal;
import lu.snt.rcore.logic.MyBoolean;
import lu.snt.rcore.logic.Rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;


public abstract class QueryServant extends Thread
        implements Servant {
    protected boolean flag = false;
    //protected String incomingPeer, outcomingPeer;
    protected QueryInterface QI;

    //protected Peer incomingPeer;
    // protected IncQueriesCache incQueriesCache;
    //protected OutQueriesCache outQueriesCache;
    //protected String servicesAddress;
    // protected String groupName;

    protected static int outOfProportion = -2;
    protected Literal literal, queryLiteral;
    protected MyBoolean localAnswer;
    //protected AnswerSet cachedAnswer;
    protected LinkedList currLocalHistory, supportingRules, inheritedHistory, bequeathHistory;
    protected LinkedList suppForLiteral, conflForLiteral;
    protected Collection inheritedSupportingRules, inheritedConflictingRules, rulesCollection;
    protected Query query;
    protected KnowledgeBase kb;
    protected Rule rule;
    protected Iterator rules, body;
    //protected FindPeerResponse findPeerMessage;
    protected QueryResponse queryResponse;
    protected int processID;
    protected Drop dr;


    // As it is passed from peerlib.handlers.ResponseHandler class

    protected void setParameters(KnowledgeBase kb, QueryInterface QI, Query q, int ID, Drop dr) {
        this.dr = dr;
        this.processID = ID;
        this.query = q;
        this.QI = QI;


        this.kb = kb;
    }

    public abstract void serve();

    public void run() {
        serve();
    }

    public abstract void handleConflictingSet() throws Throwable;

    public abstract void handleSupportingSet() throws Throwable;

    public void init() {
        currLocalHistory = new LinkedList();
        //incQueriesCache = IncQueriesCache.getInstance();
        //outQueriesCache = OutQueriesCache.getInstance();

        //adhoc = AdHocNetworkManager.getInstance(this.servicesAddress);
        //kb = KnowledgeBase.getInstance();
    }

    public void resolveConflicts()
            throws Throwable {
        System.out.print("\tSolving the conflicts: ");

        if (isStronger(inheritedSupportingRules, inheritedConflictingRules, kb.getTrustOrder()) == inheritedSupportingRules) {
            System.out.println(" the supporting set wins!\n");
            //incQueriesCache.rememberLiteral(queryLiteral, new MyBoolean(true), inheritedSupportingRules );
            QI.sendResults(new QueryResponse(query, new MyBoolean(true), inheritedSupportingRules, null), this.processID);
            return;
        } else {
            System.out.println(" the conflicting set wins!\n");
            //incQueriesCache.rememberLiteral(queryLiteral, new MyBoolean(false), inheritedConflictingRules);
            //System.out.println(inheritedConflictingRules);
            QI.sendResults(new QueryResponse(query, new MyBoolean(false), null, inheritedConflictingRules), this.processID);
            return;
        }
    }

    public Collection uniteSets(Collection setA, Collection setB) {
        Iterator iterA, iterB;
        LinkedList c;

        c = new LinkedList();

        if (setA == null)
            return (setB);
        else
            iterA = setA.iterator();

        if (setB == null)
            return (setA);
        else
            iterB = setB.iterator();

        while (iterA.hasNext())
            c.add(iterA.next());

        while (iterB.hasNext())
            c.add(iterB.next());

        //	System.out.println("gmt gmt gmt");
        return (c);

    }

    public Collection isStronger(Collection setA, Collection setB, ArrayList trust) {
        Literal rA, rB;

        rA = findWeakest(setA, trust);
        rB = findWeakest(setB, trust);

//	System.out.println(rA);
//	System.out.println(rB);

        if (isReliable(rA, rB, trust)) {
//	System.out.println(setA);
            return (setA);
        } else if (isReliable(rB, rA, trust)) {
//	System.out.println(setB);
            return (setB);
        } else
            return (null);
    }

    public boolean isReliable(Literal rA, Literal rB, ArrayList trust) {
        int seqNumA, seqNumB;

        if (rA == null)
            seqNumA = outOfProportion;
        else
            for (seqNumA = 0; seqNumA < trust.size(); seqNumA++)
                if (rA.getLocation().equals((String) trust.get(seqNumA))) break;

        if (rB == null)
            seqNumB = outOfProportion;
        else
            for (seqNumB = 0; seqNumB < trust.size(); seqNumB++)
                if (rB.getLocation().equals((String) trust.get(seqNumB))) break;

        //System.out.println("seqNUmA=" + seqNumA + " seqB=" + seqNumB);
        return (seqNumA < seqNumB ? true : false);
    }

    public Literal findWeakest(Collection setA, ArrayList trust) {
        Object[] ruleList;
        Literal rule;
        int i, j;

        ruleList = setA.toArray();

        for (i = trust.size() - 1; i >= 0; i--) {
            for (j = 0; j < ruleList.length; j++) {
                rule = (Literal) ruleList[j];
                if ((rule.getLocation()).equals((String) trust.get(i)))
                    return (rule);
            }
        }

        return (null);
    }

    public void local_alg(Literal queryLiteral, Collection localHist, MyBoolean localAnswer)
            throws Throwable {

        //System.out.println("inside local alg, solving "+queryLiteral.fullName()) ;
        Iterator body, rules, valuesIterator;
        Literal literal;
        Collection rulesCollection, literalValues;
        Rule rule;
        MyBoolean literalLocalAnswer;
        //KnowledgeBase kb = KnowledgeBase.getInstance();

        try {
            literalValues = new LinkedList();
            rulesCollection = (Collection) kb.getSignedRulesByHeadLiteral("L", queryLiteral);
        } catch (UnknownRuleException ure) {
            localAnswer.setMyBoolean(false);
            return;
        } // closed world assumption

        rules = rulesCollection.iterator();

        while (rules.hasNext()) {
            rule = (Rule) rules.next();
            body = rule.getBody();

            if (body == null) {
                localAnswer.setMyBoolean(true);
          //      System.out.println("Local alg for "+queryLiteral.fullName()+ "= true") ;
                return;    // termination condition
            }

            while (body.hasNext()) {
                literal = (Literal) body.next();

                if (localHist.contains(literal.getName()))
                    break;
                else {
                    localHist.add(new String(literal.getName()));
                    literalLocalAnswer = new MyBoolean(false);
                    local_alg(literal, localHist, literalLocalAnswer);
                //    System.out.println("Literal val add"+literal.fullName()+ literalLocalAnswer.getMyBoolean()) ;
                    literalValues.add(literalLocalAnswer);
                }
            }

            valuesIterator = literalValues.iterator();

            while (valuesIterator.hasNext())
                if (((MyBoolean) valuesIterator.next()).getMyBoolean() == false) {
                    localAnswer.setMyBoolean(false);
             //       System.out.println("Local alg for final "+queryLiteral.fullName()+ "= false") ;
                    return;
                }

            localAnswer.setMyBoolean(true);
          //  System.out.println("Local alg for final2 "+queryLiteral.fullName()+ "= true") ;
            return;
        }
    }

}
