package lu.snt.rcore.agencies;

/**
 * Created with IntelliJ IDEA.
 * User: assaad.moawad
 * Date: 15/05/13
 * Time: 14:22
 * To change this template use File | Settings | File Templates.
 */

import lu.snt.rcore.QueryInterface;
import lu.snt.rcore.knowledge.KnowledgeBase;
import lu.snt.rcore.logic.Literal;
import lu.snt.rcore.logic.MyBoolean;
import lu.snt.rcore.logic.MyTriadic;
import lu.snt.rcore.logic.Rule;

import java.util.*;

public class QueryServantStrengthOfAnswers
        extends QueryServant
        implements Runnable {
    public void serve() {
        MyTriadic answer;
        long startTime, endTime;

        try {
            init();

            startTime = (new Date()).getTime();


            queryLiteral = query.getLiteral();
            inheritedHistory = (LinkedList) query.getHistory();
            inheritedSupportingRules = query.getSupportingMappings();
            inheritedConflictingRules = query.getConflictingMappings();

            System.out.println("\n\n |||||| New query session ||||||\n");

            System.out.print("\tLocal reasoning for: " + queryLiteral.getName());

            if (kb.isRuleInside("L", queryLiteral)) {
                localAnswer = new MyBoolean(false);
                currLocalHistory.add(queryLiteral);
                local_alg(queryLiteral, currLocalHistory, localAnswer);

                if (localAnswer.getMyBoolean()) {
                    System.out.println("  Answer: " + localAnswer.getMyBoolean());
                    QI.sendResults(new QueryResponse(query, new MyTriadic(queryLiteral, MyTriadic.StrictAnswer), null, null), this.processID);
                    return;
                }
            }

            queryLiteral.reverseSign();

            if (kb.isRuleInside("L", queryLiteral)) {
                localAnswer = new MyBoolean(false);
                currLocalHistory.clear();
                currLocalHistory.add(queryLiteral);
                local_alg(queryLiteral, currLocalHistory, localAnswer);

                if (localAnswer.getMyBoolean()) {
                    localAnswer.setMyBoolean(false);
                    System.out.println("  Answer: " + localAnswer.getMyBoolean());
                    queryLiteral.reverseSign();
                    //incQueriesCache.rememberLiteral(queryLiteral, localAnswer, null);
                    queryLiteral.reverseSign();
                    QI.sendResults(new QueryResponse(query, new MyTriadic(queryLiteral, MyTriadic.NoAnswer), null, null), this.processID);
                    return;
                }
            }

            queryLiteral.reverseSign();

            ////////////////////////////////////////////////////////////////////////
            ////////////// distributed reasoning for supporting rules //////////////

            rulesCollection = (Collection) kb.getSupportingRulesByHeadLiteral(queryLiteral);
            rules = rulesCollection.iterator();

            System.out.println("\n\n\tReasoning for support of literal: " + queryLiteral.getSignWithName());
            while (rules.hasNext()) {
                flag = false;
                supportingRules = new LinkedList();

                rule = (Rule) rules.next();
                body = rule.getBody();

                bequeathHistory = new LinkedList(inheritedHistory);

                while (body.hasNext()) {
                    literal = (Literal) body.next();

                    if (inheritedHistory.contains(literal.getSignWithName())) {
                        flag = true;
                        break;
                    } else {
                        uniteSets(bequeathHistory, inheritedHistory);
                        bequeathHistory.add(literal.getSignWithName());


                        suppForLiteral = new LinkedList();
                        conflForLiteral = new LinkedList();
                        System.out.println("\tRequesting literal: " + literal.getSignWithName() + "  from: " + literal.getLocation() + "\n");

                        Query q = new Query(this.QI.getName(), this.processID, literal.getLocation(), literal, false, suppForLiteral, conflForLiteral, bequeathHistory);
                        QI.sendQueryToPeer(q);
                        queryResponse = this.dr.take();


                        answer = (MyTriadic) queryResponse.getReturnAnswer();

                        if (answer.getAnswerType() == MyTriadic.NoAnswer) {
                            flag = true;
                            break;
                        } else if (answer.getAnswerType() != MyTriadic.NoAnswer && (kb.isLocalLiteralInside(literal) == false))
                            supportingRules.add(answer);
                        else
                            supportingRules = (LinkedList) uniteSets(supportingRules, suppForLiteral);

                        //outQueriesCache.rememberLiteral(literal, queryResponse.getReturnAnswer(), supportingRules);
                    }
                }
                if (flag == true)
                    continue;
                else
                    handleSupportingSet();
            }

            if (inheritedSupportingRules.size() == 0) {
                //incQueriesCache.rememberLiteral(queryLiteral, new MyTriadic(queryLiteral, MyTriadic.NoAnswer), null);
                this.QI.sendResults(new QueryResponse(query, new MyTriadic(queryLiteral, MyTriadic.NoAnswer), null, null), this.processID);
                return;

            }

            /////////////////////////////////////////////////////////////////////////
            ////////////// distributed reasoning for conflicting rules //////////////

            rulesCollection = (Collection) kb.getConflictingRulesByHeadLiteral(queryLiteral);
            rules = rulesCollection.iterator();
            System.out.println("\tReasoning for conflicts for literal: " + queryLiteral.getSignWithName());

            while (rules.hasNext()) {
                flag = false;
                supportingRules = new LinkedList();

                rule = (Rule) rules.next();
                body = rule.getBody();

                bequeathHistory = new LinkedList(inheritedHistory);

                while (body.hasNext()) {
                    literal = (Literal) body.next();

                    if (inheritedHistory.contains(literal.getSignWithName())) {
                        flag = true;
                        break;
                    } else {
                        uniteSets(bequeathHistory, inheritedHistory);
                        bequeathHistory.add(literal.getSignWithName());


                        suppForLiteral = new LinkedList();
                        conflForLiteral = new LinkedList();
                        System.out.println("\tRequesting literal: " + literal.getSignWithName() + "  from: " + literal.getLocation() + "\n");

                        Query q = new Query(this.QI.getName(), this.processID, literal.getLocation(), literal, false, suppForLiteral, conflForLiteral, bequeathHistory);
                        QI.sendQueryToPeer(q);
                        queryResponse = this.dr.take();


                        answer = (MyTriadic) queryResponse.getReturnAnswer();

                        if (answer.getAnswerType() == MyTriadic.NoAnswer) {
                            flag = true;
                            break;
                        } else if (answer.getAnswerType() != MyTriadic.NoAnswer && (kb.isLocalLiteralInside(literal) == false))
                            supportingRules.add(answer);
                        else
                            supportingRules = (LinkedList) uniteSets(supportingRules, suppForLiteral);

                        //outQueriesCache.rememberLiteral(literal, queryResponse.getReturnAnswer(), supportingRules);
                    }
                }

                if (flag == true)
                    continue;
                else
                    handleConflictingSet();
            }

            if (inheritedConflictingRules.size() == 0) {
                // incQueriesCache.rememberLiteral(queryLiteral, new MyTriadic(queryLiteral, MyTriadic.WeakAnswer), null);
                this.QI.sendResults(new QueryResponse(query, new MyTriadic(queryLiteral, MyTriadic.WeakAnswer), inheritedSupportingRules, null), this.processID);
                return;
            }

            endTime = (new Date()).getTime();

            System.out.println("\n   Time taken: " + (endTime - startTime));

            resolveConflicts();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void handleConflictingSet()
            throws Throwable {
        if (inheritedConflictingRules.size() == 0 || this.isStronger(supportingRules, inheritedConflictingRules, kb.getTrustOrder()) == supportingRules)
            inheritedConflictingRules = supportingRules;
    }

    public void handleSupportingSet()
            throws Throwable {
        if (inheritedSupportingRules.size() == 0 || this.isStronger(supportingRules, inheritedSupportingRules, kb.getTrustOrder()) == supportingRules)
            inheritedSupportingRules = supportingRules;
    }

    public void resolveConflicts()
            throws Throwable {
        System.out.print("\tSolving the conflicts: ");

        System.out.println(inheritedSupportingRules);
        System.out.println(inheritedConflictingRules);

        if (this.isStronger(inheritedSupportingRules, inheritedConflictingRules, kb.getTrustOrder()) == inheritedSupportingRules) {
            System.out.println(" the supporting set wins!\n");
//		incQueriesCache.rememberLiteral(queryLiteral, new MyBoolean(true));
            this.QI.sendResults(new QueryResponse(query, new MyTriadic(queryLiteral, MyTriadic.WeakAnswer), inheritedSupportingRules, null), this.processID);
            return;
        } else {
            System.out.println(" the conflicting set wins!\n");
//		incQueriesCache.rememberLiteral(queryLiteral, new MyBoolean(false));
            this.QI.sendResults(new QueryResponse(query, new MyTriadic(queryLiteral, MyTriadic.NoAnswer), null, inheritedConflictingRules), this.processID);
            return;
        }
    }

    public Collection isStronger(Collection setA, Collection setB, ArrayList trust) {
        MyTriadic rA, rB;

        rA = this.weakest(setA, trust);
        rB = this.weakest(setB, trust);

//	System.out.println(rA);
//	System.out.println(rB);

        if (rA.getAnswerType() == MyTriadic.StrictAnswer && rB.getAnswerType() == MyTriadic.WeakAnswer)
            return (setA);

        if (rB.getAnswerType() == MyTriadic.StrictAnswer && rA.getAnswerType() == MyTriadic.WeakAnswer)
            return (setB);

        if (isReliable(rA, rB, trust)) {
//	System.out.println(setA);
            return (setA);
        } else if (isReliable(rB, rA, trust)) {
//	System.out.println(setB);
            return (setB);
        } else
            return (null);
    }

    public boolean isReliable(MyTriadic rA, MyTriadic rB, ArrayList trust) {
        int seqNumA, seqNumB;

        if (rA == null)
            seqNumA = outOfProportion;
        else
            for (seqNumA = 0; seqNumA < trust.size(); seqNumA++)
                if (rA.getLiteral().getLocation().equals((String) trust.get(seqNumA))) break;

        if (rB == null)
            seqNumB = outOfProportion;
        else
            for (seqNumB = 0; seqNumB < trust.size(); seqNumB++)
                if (rB.getLiteral().getLocation().equals((String) trust.get(seqNumB))) break;

        //System.out.println("seqNUmA=" + seqNumA + " seqB=" + seqNumB);
        return (seqNumA < seqNumB ? true : false);
    }

    public MyTriadic weakest(Collection set, ArrayList trust) {
        LinkedList c;

        c = (LinkedList) splitWeakest(set);

        if (c.size() == 0)
            return (this.findDaWeakest(set, trust));
        else
            return (this.findDaWeakest(c, trust));
    }

    public Collection splitWeakest(Collection set) {
        Iterator iter;
        MyTriadic answer;

        LinkedList c = new LinkedList();

        iter = set.iterator();

        while (iter.hasNext()) {
            answer = (MyTriadic) iter.next();

            if (answer.getAnswerType() == MyTriadic.WeakAnswer)
                c.add(answer);
        }

        return (c);
    }

    public MyTriadic findDaWeakest(Collection setA, ArrayList trust) {
        Object[] ruleList;
        MyTriadic answer;
        int i, j;

        ruleList = setA.toArray();

        for (i = trust.size() - 1; i >= 0; i--) {
            for (j = 0; j < ruleList.length; j++) {
                answer = (MyTriadic) ruleList[j];
                if (answer.getLiteral().getLocation().equals((String) trust.get(i)))
                    return (answer);
            }
        }

        return (null);
    }

    public QueryServantStrengthOfAnswers(KnowledgeBase kb, QueryInterface QI, Query q, int ID, Drop dr) {
        try {
            init();
            setParameters(kb, QI, q, ID, dr);
        } catch (Exception ex) {
            QI.notifyFailure(query, processID);
        }
    }


    public void run() {
        System.out.println("A Strength-Of-Answers Servant has Started on " + this.QI.getName());
        serve();
    }
}
