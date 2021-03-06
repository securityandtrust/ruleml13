package lu.snt.rcore.agencies;

/**
 * Created with IntelliJ IDEA.
 * User: assaad.moawad
 * Date: 15/05/13
 * Time: 14:23
 * To change this template use File | Settings | File Templates.
 */

import lu.snt.rcore.QueryInterface;
import lu.snt.rcore.knowledge.KnowledgeBase;
import lu.snt.rcore.logic.Literal;
import lu.snt.rcore.logic.MyBoolean;
import lu.snt.rcore.logic.Rule;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

public class QueryServantPropSets
        extends QueryServant
        implements Runnable {
    public void serve() {

        long startTime, endTime;

        try {
            startTime = (new Date()).getTime();
            queryLiteral = query.getLiteral();
            inheritedHistory = (LinkedList) query.getHistory();
            inheritedSupportingRules = query.getSupportingMappings();
            inheritedConflictingRules = query.getConflictingMappings();

            System.out.println("\n\n |||||| New query session ||||||\n");

            System.out.print("\tLocal reasoning for: " + queryLiteral.getSignWithName());

/*	  if((cachedAnswer=incQueriesCache.getAnswerForLiteral(queryLiteral))!=null)
      {
		System.out.println(" is already cached: " + ((MyBoolean)cachedAnswer.getAnswer()).getMyBoolean() );
        incomingPeer.send(new QueryResponse(cachedAnswer.getAnswer(), cachedAnswer.getSet(), null));
		return;
	  }
*/
            if (kb.isRuleInside("L", queryLiteral)) {
                localAnswer = new MyBoolean(false);
                currLocalHistory.add(queryLiteral);
                local_alg(queryLiteral, currLocalHistory, localAnswer);

                if (localAnswer.getMyBoolean()) {
                    System.out.println("  Answer: " + localAnswer.getMyBoolean());
//			incQueriesCache.rememberLiteral(queryLiteral, localAnswer, null);
                    QI.sendResults(new QueryResponse(query, localAnswer, null, null), this.processID);
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
//			incQueriesCache.rememberLiteral(queryLiteral, localAnswer, null);
                    queryLiteral.reverseSign();
                    QI.sendResults(new QueryResponse(query, localAnswer, null, null), this.processID);
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
                        System.out.println("\tRequesting literal: " + literal.getName() + "  from: " + literal.getLocation() + "\n");

                        Query q = new Query(this.QI.getName(), this.processID, false, literal.getLocation(), literal, false, suppForLiteral, conflForLiteral, bequeathHistory);
                        QI.sendQueryToPeer(q);
                        queryResponse = this.dr.take();

//				outQueriesCache.rememberLiteral(literal, new MyBoolean(queryResponse.getReturnValue()), queryResponse.getSupportingSet() );

                        if (queryResponse.getReturnValue() == false) {
                            flag = true;
                            break;
                        } else if (queryResponse.getReturnValue() == true && (kb.isLocalLiteralInside(literal) == false)) {
                            if (queryResponse.getSupportingSet() == null)
                                suppForLiteral = new LinkedList();
                            else
                                suppForLiteral = new LinkedList(queryResponse.getSupportingSet());

                            suppForLiteral.add(literal);
                            supportingRules = (LinkedList) uniteSets(supportingRules, suppForLiteral);
                        } else
                            supportingRules = (LinkedList) uniteSets(supportingRules, queryResponse.getSupportingSet());

                        // outQueriesCache.rememberLiteral(literal, new MyBoolean(queryResponse.getReturnValue()), supportingRules);


                    }
                }

                if (flag == true)
                    continue;
                else
                    handleSupportingSet();
            }

            if (inheritedSupportingRules.size() == 0) {
//		incQueriesCache.rememberLiteral(queryLiteral, new MyBoolean(false), null);
                this.QI.sendResults(new QueryResponse(query, new MyBoolean(false), null, null), this.processID);
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
                        System.out.println("\tRequesting literal: " + literal.getName() + "  from: " + literal.getLocation() + "\n");

                        Query q = new Query(this.QI.getName(), this.processID, false, literal.getLocation(), literal, false, suppForLiteral, conflForLiteral, bequeathHistory);
                        QI.sendQueryToPeer(q);
                        queryResponse = this.dr.take();


//				outQueriesCache.rememberLiteral(literal, new MyBoolean(queryResponse.getReturnValue()), queryResponse.getSupportingSet() );

                        if (queryResponse.getReturnValue() == false) {
                            flag = true;
                            break;
                        } else if (queryResponse.getReturnValue() == true && (kb.isLocalLiteralInside(literal) == false)) {
                            if (queryResponse.getSupportingSet() == null)
                                suppForLiteral = new LinkedList();
                            else
                                suppForLiteral = new LinkedList(queryResponse.getSupportingSet());

                            suppForLiteral.add(literal);
                            supportingRules = (LinkedList) uniteSets(supportingRules, suppForLiteral);
                        } else
                            supportingRules = (LinkedList) uniteSets(supportingRules, queryResponse.getSupportingSet());

                        //  outQueriesCache.rememberLiteral(literal, new MyBoolean(queryResponse.getReturnValue()), supportingRules);

                    }
                }
                if (flag == true)
                    continue;
                else
                    handleConflictingSet();
            }

            if (inheritedConflictingRules.size() == 0) {
//		incQueriesCache.rememberLiteral(queryLiteral, new MyBoolean(true), inheritedSupportingRules);
                this.QI.sendResults(new QueryResponse(query, new MyBoolean(true), inheritedSupportingRules, null), this.processID);
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
        if (inheritedConflictingRules.size() == 0 || isStronger(supportingRules, inheritedConflictingRules, kb.getTrustOrder()) == supportingRules)
            inheritedConflictingRules = supportingRules;
    }

    public void handleSupportingSet()
            throws Throwable {
        if (inheritedSupportingRules.size() == 0 || isStronger(supportingRules, inheritedSupportingRules, kb.getTrustOrder()) == supportingRules)
            inheritedSupportingRules = supportingRules;
    }

    public QueryServantPropSets(KnowledgeBase kb, QueryInterface QI, Query q, int ID, Drop dr) {
        try {
            init();
            setParameters(kb, QI, q, ID, dr);
        } catch (Exception ex) {
            QI.notifyFailure(query, processID);
        }
    }


    public void run() {
        System.out.println("A Propagating-Mapping-Sets Servant has Started on " + this.QI.getName());
        serve();
    }
}

