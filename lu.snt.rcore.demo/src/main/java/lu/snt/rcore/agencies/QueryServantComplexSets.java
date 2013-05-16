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

import java.util.*;


public class QueryServantComplexSets
        extends QueryServant
        implements Runnable {
    public void serve() {
        LinkedList setA, setB;
        long startTime, endTime;
        Literal suppStrongLiteral, conflStrongLiteral;

        try {
            startTime = (new Date()).getTime();

            queryLiteral = query.getLiteral();
            inheritedHistory = (LinkedList) query.getHistory();
            inheritedSupportingRules = query.getSupportingMappings();
            inheritedConflictingRules = query.getConflictingMappings();

            System.out.println("\n\n |||||| New query session ||||||\n");

            System.out.print("\tLocal reasoning for: " + queryLiteral.getSignWithName());

          /*  if((cachedAnswer=incQueriesCache.getAnswerForLiteral(queryLiteral))!=null)
            {
                System.out.println(" is already cached: " + ((MyBoolean)cachedAnswer.getAnswer()).getMyBoolean() );
                if(queryLiteral.getSign()==true)
                    incomingPeer.send(new QueryResponse(cachedAnswer.getAnswer(), cachedAnswer.getSet(), null));
                else
                    incomingPeer.send(new QueryResponse(cachedAnswer.getAnswer(), null, cachedAnswer.getSet()));

                return;
            }

            queryLiteral.reverseSign();

            if((cachedAnswer=incQueriesCache.getAnswerForLiteral(queryLiteral))!=null)
            {
                System.out.println(" is already cached: " + ((MyBoolean)cachedAnswer.getAnswer()).getMyBoolean() );
                if(queryLiteral.getSign()==true)
                    incomingPeer.send(new QueryResponse(cachedAnswer.getAnswer(), null, cachedAnswer.getSet()));
                else
                    incomingPeer.send(new QueryResponse(cachedAnswer.getAnswer(), cachedAnswer.getSet(), null));

                return;
            }

            queryLiteral.reverseSign();
            */

            if (kb.isRuleInside("L", queryLiteral)) {
                localAnswer = new MyBoolean(false);
                currLocalHistory.add(queryLiteral);
                local_alg(queryLiteral, currLocalHistory, localAnswer);

                if (localAnswer.getMyBoolean()) {
                    System.out.println("  Answer: " + localAnswer.getMyBoolean());
                    //incQueriesCache.rememberLiteral(queryLiteral, localAnswer, null);
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
                    //incQueriesCache.rememberLiteral(queryLiteral, localAnswer, null);
                    queryLiteral.reverseSign();
                    QI.sendResults(new QueryResponse(query, localAnswer, null, null), this.processID);
                    return;
                }
            }

            queryLiteral.reverseSign();

            ////////////////////////////////////////////////////////////////////////
            ////////////// distributed reasoning for supporting rules //////////////

            System.out.println("\n\n\tReasoning for support of literal: " + queryLiteral.getSignWithName());
            rulesCollection = (Collection) kb.getSupportingRulesByHeadLiteral(queryLiteral);
            rules = rulesCollection.iterator();

            while (rules.hasNext()) {
                flag = false;
                supportingRules = new LinkedList();
                supportingRules.add(new LinkedList());

                rule = (Rule) rules.next();
                body = rule.getBody();

                bequeathHistory = new LinkedList(inheritedHistory);

                while (body.hasNext()) {
                    literal = (Literal) body.next();

                    if (inheritedHistory.contains(literal.getSignWithName())) {
                        flag = true;
                        break;
                    } else {
                        super.uniteSets(bequeathHistory, inheritedHistory);
                        bequeathHistory.add(literal.getSignWithName());


                        //findPeerMessage = adhoc.findPeer(literal.getLocation(), this.groupName);
                        // outcomingPeer = new Peer(literal.getLocation(), findPeerMessage.getIPAddress(), findPeerMessage.getPort());

                        suppForLiteral = new LinkedList();
                        conflForLiteral = new LinkedList();
                        System.out.println("\tRequesting literal: " + literal.getSignWithName() + "  from: " + literal.getLocation() + "\n");
                        Query q = new Query(this.QI.getName(), this.processID, literal.getLocation(), literal, false, suppForLiteral, conflForLiteral, bequeathHistory);
                        QI.sendQueryToPeer(q);
                        queryResponse = this.dr.take();

                        if (queryResponse.getReturnValue() == false) {
                            flag = true;
                            break;
                        } else if (queryResponse.getReturnValue() == true && (kb.isLocalLiteralInside(literal) == false)) {
                            LinkedList c = new LinkedList();
                            LinkedList d = new LinkedList();

                            c.add(literal);
                            d.add(c);

                            if (queryResponse.getSupportingSet() == null) {
                                suppForLiteral = new LinkedList();
                                suppForLiteral.add(new LinkedList());
                                supportingRules = (LinkedList) this.uniteSet(supportingRules, (LinkedList) this.uniteSet(suppForLiteral, d));
                            } else {
                                supportingRules = (LinkedList) this.uniteSet(supportingRules, (LinkedList) this.uniteSet(queryResponse.getSupportingSet(), d));
                            }

                            System.out.println("  1-" + supportingRules);

                            //outQueriesCache.rememberLiteral(literal, new MyBoolean(queryResponse.getReturnValue()), supportingRules);
                        } else {
                            LinkedList c;

                            if (queryResponse.getSupportingSet() == null) {
                                c = new LinkedList();
                                c.add(new LinkedList());
                                supportingRules = (LinkedList) this.uniteSet(supportingRules, c);
                            } else
                                supportingRules = (LinkedList) this.uniteSet(supportingRules, queryResponse.getSupportingSet());

                            System.out.println("  2-" + supportingRules);

                            //  outQueriesCache.rememberLiteral(literal, new MyBoolean(queryResponse.getReturnValue()), supportingRules);

                        }

                    }
                }

                if (flag == true)
                    continue;
                else
                    handleSupportingSet();
            }

            if (inheritedSupportingRules.size() == 1 || inheritedSupportingRules.size() == 0) {
                LinkedList tmp;

                if (inheritedSupportingRules.size() != 0)
                    tmp = (LinkedList) ((LinkedList) inheritedSupportingRules).get(0);
                else
                    tmp = new LinkedList();

                if (tmp.size() == 0) {
                    //incQueriesCache.rememberLiteral(queryLiteral, new MyBoolean(false), null);
                    this.QI.sendResults(new QueryResponse(query, new MyBoolean(false), null, null), this.processID);
                    return;
                }
            }


            /////////////////////////////////////////////////////////////////////////
            ////////////// distributed reasoning for conflicting rules //////////////

            System.out.println("\tReasoning for conflicts for literal: " + queryLiteral.getSignWithName());
            rulesCollection = (Collection) kb.getConflictingRulesByHeadLiteral(queryLiteral);
            rules = rulesCollection.iterator();

            while (rules.hasNext()) {
                flag = false;
                supportingRules = new LinkedList();
                supportingRules.add(new LinkedList());

                rule = (Rule) rules.next();
                body = rule.getBody();

                bequeathHistory = new LinkedList(inheritedHistory);

                while (body.hasNext()) {
                    literal = (Literal) body.next();

                    if (inheritedHistory.contains(literal.getSignWithName())) {
                        flag = true;
                        break;
                    } else {

                        super.uniteSets(bequeathHistory, inheritedHistory);
                        bequeathHistory.add(literal.getSignWithName());


                        suppForLiteral = new LinkedList();
                        conflForLiteral = new LinkedList();
                        System.out.println("\tRequesting literal: " + literal.getSignWithName() + "  from: " + literal.getLocation() + "\n");

                        Query q = new Query(this.QI.getName(), this.processID, literal.getLocation(), literal, false, suppForLiteral, conflForLiteral, bequeathHistory);
                        QI.sendQueryToPeer(q);
                        queryResponse = this.dr.take();


                        if (queryResponse.getReturnValue() == false) {
                            flag = true;
                            break;
                        } else if (queryResponse.getReturnValue() == true && (kb.isLocalLiteralInside(literal) == false)) {
                            LinkedList c = new LinkedList();
                            LinkedList d = new LinkedList();

                            c.add(literal);
                            d.add(c);

                            if (queryResponse.getSupportingSet() == null) {
                                suppForLiteral = new LinkedList();
                                suppForLiteral.add(new LinkedList());
                                supportingRules = (LinkedList) this.uniteSet(supportingRules, (LinkedList) this.uniteSet(suppForLiteral, d));
                            } else
                                supportingRules = (LinkedList) this.uniteSet(supportingRules, (LinkedList) this.uniteSet(queryResponse.getSupportingSet(), d));


                            System.out.println("  3-" + supportingRules);

                            // outQueriesCache.rememberLiteral(literal, new MyBoolean(queryResponse.getReturnValue()), supportingRules);
                        } else {
                            LinkedList c;

                            if (queryResponse.getSupportingSet() == null) {
                                c = new LinkedList();
                                c.add(new LinkedList());
                                supportingRules = (LinkedList) this.uniteSet(supportingRules, c);
                            } else
                                supportingRules = (LinkedList) this.uniteSet(supportingRules, queryResponse.getSupportingSet());

                            System.out.println("  4-" + supportingRules);

                            //outQueriesCache.rememberLiteral(literal, new MyBoolean(queryResponse.getReturnValue()), supportingRules);
                        }

                    }
                }

                if (flag == true)
                    continue;
                else
                    handleConflictingSet();
            }

            if (inheritedConflictingRules.size() == 1 || inheritedConflictingRules.size() == 0) {
                LinkedList tmp;

                if (inheritedConflictingRules.size() != 0)
                    tmp = (LinkedList) ((LinkedList) inheritedConflictingRules).get(0);
                else
                    tmp = new LinkedList();

                if (tmp.size() == 0) {

                    this.QI.sendResults(new QueryResponse(query, new MyBoolean(true), inheritedSupportingRules, null), this.processID);
                    return;
                }
            }

            ////////////////////////////////////////////////////////////////////

            setA = new LinkedList();
            setB = new LinkedList();

            setA = weakest((LinkedList) inheritedSupportingRules, kb.getTrustOrder());
            setB = weakest((LinkedList) inheritedConflictingRules, kb.getTrustOrder());

            suppStrongLiteral = findStrongest(setA, kb.getTrustOrder());
            conflStrongLiteral = findStrongest(setB, kb.getTrustOrder());

            setA = new LinkedList();
            setB = new LinkedList();

            setA.add(suppStrongLiteral);
            setB.add(conflStrongLiteral);

            System.out.print("\tSolving the conflicts: ");

            endTime = (new Date()).getTime();

            System.out.println("\n   Time taken: " + (endTime - startTime));

            if (isStronger(setA, setB, kb.getTrustOrder()) == setA) {
                System.out.println(" the supporting set wins!\n");
                //incQueriesCache.rememberLiteral(queryLiteral, new MyBoolean(true), inheritedSupportingRules);

                this.QI.sendResults(new QueryResponse(query, new MyBoolean(true), inheritedSupportingRules, null), this.processID);
                return;
            } else {
                System.out.println(" the conflicting set wins!\n");
                //incQueriesCache.rememberLiteral(queryLiteral, new MyBoolean(false), inheritedConflictingRules);
                this.QI.sendResults(new QueryResponse(query, new MyBoolean(false), null, inheritedConflictingRules), this.processID);
                return;
            }


        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public LinkedList weakest(LinkedList set, ArrayList trust) {
        LinkedList c = new LinkedList();

        if (set.getFirst() instanceof Literal)
            c.add(findWeakest(set, trust));
        else
            for (int i = 0; i < set.size(); i++)
                c.add(findWeakest((LinkedList) ((LinkedList) set).get(i), trust));

        return (c);
    }

    public static Literal findStrongest(Collection setA, ArrayList trust) {
        Object[] ruleList;
        Literal rule;
        int i, j;

        ruleList = setA.toArray();

        for (i = 0; i < trust.size(); i++) {
            for (j = 0; j < ruleList.length; j++) {
                rule = (Literal) ruleList[j];
                if ((rule.getLocation()).equals((String) trust.get(i)))
                    return (rule);
            }
        }

        return (null);
    }

    public Collection uniteSet(Collection sA, Collection sB) {
        Iterator iterA, iterB;
        LinkedList c, smallSetA, smallSetB, currSet, setA, setB;

        setA = (LinkedList) sA;
        setB = (LinkedList) sB;

        c = new LinkedList();

        if (setA.size() == 1) {
            if (((LinkedList) setA.get(0)).size() == 0)
                return (setB);
        }

        if (setB.size() == 1) {
            if (((LinkedList) setB.get(0)).size() == 0)
                return (setA);
        }

        iterA = setA.iterator();

        while (iterA.hasNext()) {
            currSet = new LinkedList();
            smallSetA = (LinkedList) iterA.next();

            iterB = setB.iterator();

            while (iterB.hasNext()) {
                smallSetB = (LinkedList) iterB.next();
                currSet = (LinkedList) super.uniteSets(smallSetA, smallSetB);
            }

            c.add(currSet);
        }

        return (c);
    }

    public void handleConflictingSet()
            throws Throwable {
        inheritedConflictingRules = (LinkedList) super.uniteSets(inheritedConflictingRules, supportingRules);
    }

    public void handleSupportingSet()
            throws Throwable {
        inheritedSupportingRules = (LinkedList) super.uniteSets(inheritedSupportingRules, supportingRules);
        ;
    }


    public QueryServantComplexSets(KnowledgeBase kb, QueryInterface QI, Query q, int ID, Drop dr) {
        try {
            init();
            setParameters(kb, QI, q, ID, dr);
        } catch (Exception ex) {
            QI.notifyFailure(query, processID);
        }
    }


    public void run() {
        System.out.println("A Complex-Sets-query Servant has Started on " + this.QI.getName());
        serve();
    }
}

