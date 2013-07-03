package lu.snt.rcore.knowledge;

/**
 * Created with IntelliJ IDEA.
 * User: assaad.moawad
 * Date: 15/05/13
 * Time: 14:30
 * To change this template use File | Settings | File Templates.
 */

import lu.snt.rcore.QueryInterface;
import lu.snt.rcore.logic.Literal;
import lu.snt.rcore.logic.Rule;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;


// Singleton class
public class KnowledgeBase
        implements KnowledgeBaseInterface {
    public static final String localSetName = "L";
    public static final String remoteSetName = "M";

    private Hashtable knowledgeSet;
    private ArrayList trustOrder;
    private ArrayList localLiteralList;
    private ArrayList remoteLiteralList;

    public KnowledgeBase() {
        knowledgeSet = new Hashtable();
        localLiteralList = new ArrayList();
        remoteLiteralList = new ArrayList();
        //System.out.println("knowledgeSet created");
    }

    public String getInfo() {
        return knowledgeSet.size() + "";
    }


    public void setTrustOrder(String s) {
        String [] sArr = s.split(",");
        ArrayList c = new ArrayList();

        for(int i=0;i<sArr.length;i++){
            c.add(sArr[i].trim());
        }
        this.trustOrder = new ArrayList();
        this.trustOrder.addAll(c);
        //System.out.println(this.trustOrder.size());
    }

    public void setTrustOrder(Collection c) {
        this.trustOrder = new ArrayList();
        this.trustOrder.addAll(c);
        //System.out.println(this.trustOrder.size());
    }

    public ArrayList getTrustOrder() {
        return (this.trustOrder);
    }


    public void createNewSet(String setName)
            throws Throwable {
        if (knowledgeSet.containsKey(setName))
            throw new ExistingSetException();

        knowledgeSet.put(setName, new Hashtable());
        // System.out.println("Added sse" + setName);
    }


    public void AddLine(String processedLine, String peerName) throws Throwable {

        try {

            String literal;
        String[] lineParticles, ruleParts, body, literalString, tmpArray, tmpArray2;
        LinkedList bodyList;
        int i;
        String rule;
        boolean sign;
        char setName[] = new char[1];

        if (processedLine.length() >= 5) {
            if (processedLine.charAt(0) != '#') {
                lineParticles = processedLine.split(":");
                lineParticles[0]=lineParticles[0].trim();
                rule = lineParticles[1].trim();
                ruleParts = rule.split("->");
                ruleParts[0] = ruleParts[0].trim();
                ruleParts[1] = ruleParts[1].trim();

               // System.out.println(ruleParts[0].length());
                if (ruleParts[0].length() == 0) {

                    sign = getSign(ruleParts[1]);
                   // System.out.println(" * " + ruleParts[1] + " " + sign);
                    literalString = ruleParts[1].split("~");
                    literal = ((literalString.length > 1) ? literalString[1] : literalString[0]);
                    tmpArray2 = literal.split("_");

                  //  System.out.println(" + " + literal + " " + sign);


                    if (tmpArray2[1].equals("local")) {
                        this.addLocalLiteral(new Literal(tmpArray2[0], tmpArray2[1], sign));
                   //      System.out.println("p " + tmpArray2[0]);
                    } else if (tmpArray2[1].equals(peerName)) {
                        this.addRemoteLiteral(new Literal(tmpArray2[0], tmpArray2[1], sign));
                    }

                    setName[0]  = lineParticles[0].charAt(0);
                    if( setName[0]!='L' &&setName[0]!='M' )
                        throw new Exception("Invalid set Name");
                   // System.out.println("sets :"+ setName[0]+"");
                    this.addRule((setName[0] + "").trim(), new Rule(new Literal(tmpArray2[0], tmpArray2[1], sign), lineParticles[0], lineParticles[0]));

                  //  System.out.println(" -p  setName=" + setName[0] + " " + tmpArray2[0] +" "+ tmpArray2[1] + " "+sign);
                } else {
                    body = ruleParts[0].split(",");
                    bodyList = new LinkedList();

                    for (i = 0; i < body.length; i++) {
                        body[i] = body[i].trim();
                        sign = getSign(body[i]);
                        literalString = body[i].split("~");
                        literal = ((literalString.length > 1) ? literalString[1] : literalString[0]);
                        tmpArray = literal.split("_");
                        bodyList.add(new Literal(tmpArray[0], tmpArray[1], sign));

                    //     System.out.println(" + " + literal + " " + sign);

                        if (tmpArray[1].equals("local")) {
                            addLocalLiteral(new Literal(tmpArray[0], tmpArray[1], sign));
                      //      System.out.println(tmpArray[0]);
                        } else if (tmpArray[1].equals(peerName)) {
                            this.addRemoteLiteral(new Literal(tmpArray[0], tmpArray[1], sign));
                        }


                     //    System.out.println(" --" + tmpArray[0] +" "+ tmpArray[1] + " "+sign);
                    }

                    literalString = null;
                    ruleParts[0] = ruleParts[0].trim();
                    ruleParts[1] = ruleParts[1].trim();

                    sign = getSign(ruleParts[1]);
                  //   System.out.println(" _ " + ruleParts[1] + " " + sign);
                    literalString = ruleParts[1].split("~");
                    literal = ((literalString.length > 1) ? literalString[1] : literalString[0]);
                    tmpArray2 = literal.split("_");

                  //   System.out.println(" + " + literal + " " + sign);


                    if (tmpArray2[1].equals("local")) {
                        addLocalLiteral(new Literal(tmpArray2[0], tmpArray2[1], sign));
                 //        System.out.println(tmpArray2[0]);
                    } else if (tmpArray2[1].equals(peerName)) {
                        this.addRemoteLiteral(new Literal(tmpArray2[0], tmpArray2[1], sign));
                    }

                  //       System.out.println(" __" + tmpArray2[0] +" "+ tmpArray2[1] + " "+sign);
                  //       System.out.println(sign + " " + ((literalString.length>1) ? literalString[1]:literalString[0]));

                    setName[0] = lineParticles[0].charAt(0);
                 //       System.out.println("Before Error");
                    addRule((setName[0] + "").trim(), new Rule(new Literal(tmpArray2[0], tmpArray2[1], sign), lineParticles[0],
                            lineParticles[0], bodyList));
                  //       System.out.println("After Error");
                 //    System.out.println(" --  setName=" + setName[0] + " " + tmpArray2[0] +" "+ tmpArray2[1] + " "+sign);
                     // System.out.println("File "+ filename+ " loaded successfully");
                }
            }
        }


        //kb.print();
        }catch (Throwable t) {
        throw(t);
    }
    }

    public void loadKbFromFile(String filename, String peerName) {
        BufferedReader br;
        String processedLine;

        try {
            br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(filename)));
            //br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            while (br.ready()) {
                processedLine = br.readLine();
                AddLine(processedLine, peerName);
            }

            //kb.print();
        } catch (Throwable t) {
            //t.printStackTrace();
        }
    }




    public void printTrustOrder(QueryInterface qr){
        if(trustOrder!=null && trustOrder.size()!=0)
        {
            String s="";
            Iterator iter = trustOrder.iterator();
            while(iter.hasNext())
            {
                s+= (String) iter.next() +" ,";
            }
            s=s.substring(0,s.length()-2);
            qr.consoleOutput(s);

        }
        else
        {
            qr.consoleOutput("No trust order found");
        }
    }


    public void loadTrustOrderFromFile(String filename) {
        BufferedReader br;
        ArrayList trustOrder = new ArrayList();
        String processedLine;

        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            while (br.ready()) {
                processedLine = br.readLine();
                if (processedLine.length() > 2) {
                    trustOrder.add(processedLine);
                }
            }
            this.setTrustOrder(trustOrder);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    private static boolean getSign(String s) {
        if (!s.startsWith("~"))
            return (true);
        else
            return (false);
    }


    public String getAllLiteral() {
        Iterator iter;
        Literal literal;
        String s = "";
        iter = localLiteralList.iterator();

        if (iter.hasNext()) {
            s += "Local Literals: ";
            while (iter.hasNext()) {
                literal = (Literal) iter.next();
                s += literal + " , ";
            }
            s = s.substring(0, s.length() - 2);
            s += "\n";
        }
        iter = remoteLiteralList.iterator();
        if (iter.hasNext()) {
            s += "Remote Literals: ";
            while (iter.hasNext()) {
                literal = (Literal) iter.next();
                s += literal + " , ";
            }
            s = s.substring(0, s.length() - 2);
            s += "\n";
        }

        return s;
    }


    public void print() {
        Iterator iter;
        Literal literal;

        iter = localLiteralList.iterator();

        while (iter.hasNext()) {
            literal = (Literal) iter.next();
            System.out.println(literal);
        }
    }

    public void addRule(String setName, Rule rule)
            throws Throwable {
        //System.out.println("Setname Received "+ setName);
        Hashtable h;
        Rule storedRule;
        LinkedList rules;

        if (knowledgeSet.containsKey(setName)) {
            //System.out.println("SetName exists : "+ setName);
            h = (Hashtable) knowledgeSet.get(setName);
            // System.out.println(setName + " loaded, " + h.size());
        } else {
            //System.out.println("Creating setname "+ setName);
            createNewSet(setName);
            h = (Hashtable) knowledgeSet.get(setName);

        }

        rules = (LinkedList) h.get(rule.getHeadName());

        if (rules != null) {
            // System.out.println("Rules non null");
            storedRule = (Rule) rules.getFirst();
            rules.addFirst(rule);
            h.put(rule.getHeadName(), rules);
        } else {
            //System.out.println("Rules null ");
            rules = new LinkedList();
            rules.addFirst(rule);
            //System.out.println(rule.getHeadName());
            h.put(rule.getHeadName(), rules);
        }
    }


    public void addLocalLiteral(Literal literal) {
        if (isLocalLiteralInside(literal) == false)
            localLiteralList.add(literal);
    }

    public void addRemoteLiteral(Literal literal) {
        if (isRemoteLiteralInside(literal) == false)
            remoteLiteralList.add(literal);
    }

    public boolean isLocalLiteralInside(Literal l) {
        Iterator iter;
        Literal literal;

        iter = localLiteralList.iterator();

        while (iter.hasNext()) {
            literal = (Literal) iter.next();
            //System.out.println(literal.getName());
            if ((literal.getName()).equals(l.getName()) && (literal.getSign() == l.getSign())) {
                //System.out.println(literal.getName());
                return (true);
            }
        }

        return (false);
    }

    public boolean isRemoteLiteralInside(Literal l) {
        Iterator iter;
        Literal literal;

        iter = remoteLiteralList.iterator();

        while (iter.hasNext()) {
            literal = (Literal) iter.next();
            //System.out.println(literal.getName());
            if ((literal.getName()).equals(l.getName()) && (literal.getSign() == l.getSign())) {
                //System.out.println(literal.getName());
                return (true);
            }
        }

        return (false);
    }

    public void removeRule(QueryInterface qr, String rNum){
        if(rNum.charAt(0)=='L'||rNum.charAt(0)=='M')
        {
            String cc = rNum.substring(0,1);
            rNum=rNum.substring(1,rNum.length());
            int r;
            try
            {
                  r=Integer.parseInt(rNum);

                int counter;
                Rule rule;
                String s;
                Hashtable h;
                LinkedList rules;
                counter=0;
                if (knowledgeSet.containsKey(cc)) {
                    h = (Hashtable) knowledgeSet.get(cc);
                    Iterator iter = h.values().iterator();
                    while (iter.hasNext()) {
                        rules= (LinkedList)   iter.next();
                        Iterator iter2 = rules.iterator();
                        while (iter2.hasNext()) {
                            if(counter==r)
                            {
                                rule = (Rule) iter2.next();
                                rules.remove(rule) ;
                                qr.consoleOutput("Rule Removed!");
                                return;
                            }
                            else
                            {
                                iter2.next();
                            }

                            counter++;
                        }

                    }
                    qr.consoleOutput("Cannot find the rule!");
                    return;
                }

            }
            catch(Exception x)
            {
                qr.consoleOutput("Failed to Remove");
                return;
            }

        }
        else if (rNum.charAt(0)=='M')
        {
            rNum=rNum.substring(1,rNum.length());
            int r;
            try
            {
                r=Integer.parseInt(rNum);

            }
            catch(Exception x)
            {
                qr.consoleOutput("Failed to Remove");
                return;
            }
        }
        else
        {
            qr.consoleOutput("Invalid arguments");
            return;
        }

    }

    public void listRules(QueryInterface qr)
    {
        int counter;
        Rule rule;
        String s;
        Hashtable h;
        LinkedList rules;
        boolean tag;


        tag=true;
        counter=0;
        if (knowledgeSet.containsKey("L")) {
            h = (Hashtable) knowledgeSet.get("L");
            Iterator iter = h.values().iterator();
            while (iter.hasNext()) {
                rules= (LinkedList)   iter.next();
                Iterator iter2 = rules.iterator();
                while (iter2.hasNext()) {
                    if(tag)
                    {
                        qr.consoleOutput("Local rules:");
                        tag=false;
                    }
                 rule = (Rule) iter2.next();
                    s="L"+counter+": "+rule.print();
                    qr.consoleOutput(s);
                    counter++;
                }
            }
        }

        tag=true;
        counter=0;
        if (knowledgeSet.containsKey("M")) {
            h = (Hashtable) knowledgeSet.get("M");
            Iterator iter = h.values().iterator();
            while (iter.hasNext()) {
                rules= (LinkedList)   iter.next();
                Iterator iter2 = rules.iterator();
                while (iter2.hasNext()) {
                    if(tag)
                    {
                        qr.consoleOutput("Remote rules:");
                        tag=false;
                    }
                    rule = (Rule) iter2.next();
                    s="M"+counter+": "+rule.print();
                    qr.consoleOutput(s);
                    counter++;
                }
            }
        }


    }

    public Collection getSupportingRulesByHeadLiteral(Literal l)
            throws Throwable {
        Collection setA, setB;
        LinkedList r, rules;
        Rule rule;
        Iterator iter;
        Literal literal;

        setA = getRulesByHeadLiteral(localSetName, l);
        setB = getRulesByHeadLiteral(remoteSetName, l);

        uniteSets(setA, setB);

        iter = setA.iterator();
        rules = new LinkedList();

        while (iter.hasNext()) {
            rule = (Rule) iter.next();
            literal = rule.getHead();
            if (literal.getSign() == l.getSign()) {
                //	System.out.println(" ___" + literal.getSignWithName());
                rules.add(rule);
            }
        }

        return (rules);
    }

    public Collection getConflictingRulesByHeadLiteral(Literal l)
            throws Throwable {
        Collection setA, setB;
        Hashtable h;
        LinkedList r, rules;
        Rule rule;
        Iterator iter;
        Literal literal;

        setA = getRulesByHeadLiteral(localSetName, l);
        setB = getRulesByHeadLiteral(remoteSetName, l);

        uniteSets(setA, setB);

        iter = setA.iterator();
        rules = new LinkedList();

        while (iter.hasNext()) {
            rule = (Rule) iter.next();
            literal = rule.getHead();
            if (literal.getSign() != l.getSign()) {
                //System.out.println(" ___" + literal.getSignWithName());
                rules.add(rule);
            }
        }

        return (rules);
    }

    public Collection getRulesByHeadLiteral(String setName, Literal l)
            throws Throwable {
        Hashtable h;
        Rule storedRule;
        LinkedList rules;
        Iterator r;

        h = (Hashtable) knowledgeSet.get(setName);
        rules = (LinkedList) h.get(l.getName());

        if (rules == null)
            return (new LinkedList());

        return (rules);
    }

    public boolean isRuleInside(String setName, Literal l) {
        Hashtable h;
        LinkedList rules;
        Rule storedRule;

        //System .out .println("Inside isRuleInside setName "+ setName+" "+ l.getName() );
        h = (Hashtable) knowledgeSet.get(setName);

        if (h == null)
            return (false);

        rules = (LinkedList) h.get(l.getName());

        if (rules == null) {
            //System .out .println("null rules ss");
            return (false);
        }

           try
           {
        storedRule = (Rule) rules.getFirst();


        if ((storedRule.getHead()).getSign() == l.getSign())
            return (true);
        else
            return (false);
           }
           catch(Exception x )
           {
               return false;
           }

    }



    // SetA is the augmented one
    public void uniteSets(Collection setA, Collection setB) {
        Iterator iter;

        iter = setB.iterator();

        while (iter.hasNext())
            setA.add(iter.next());

    }

}
