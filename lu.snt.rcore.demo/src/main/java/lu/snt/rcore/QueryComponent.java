package lu.snt.rcore;

/**
 * Created with IntelliJ IDEA.
 * User: assaad.moawad
 * Date: 15/05/13
 * Time: 14:33
 * To change this template use File | Settings | File Templates.
 */

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.framework.MessagePort;
import org.kevoree.framework.service.handler.ModelListenerAdapter;
import lu.snt.rcore.agencies.*;
import lu.snt.rcore.knowledge.KnowledgeBase;
import lu.snt.rcore.logic.Literal;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

@Provides({

        @ProvidedPort(name = "QueryIn", type = PortType.MESSAGE),
        @ProvidedPort(name = "ConsoleIn", type = PortType.MESSAGE)
})

@Requires({
        @RequiredPort(name = "QueryOut", type = PortType.MESSAGE, optional = true),
        @RequiredPort(name = "ConsoleOut", type = PortType.MESSAGE, optional = true)
})

@DictionaryType({
        @DictionaryAttribute(name = "Name", optional = false),
        @DictionaryAttribute(name = "InitialKnowledgeBaseFile", optional = true),
        @DictionaryAttribute(name = "InitialTrustFile", optional = true)

})
//((MessagePort)getPortByName("QueryOut")).process(object data);

@ComponentType
@Library(name = "Serval_RN12")
public class QueryComponent extends org.kevoree.framework.AbstractComponentType implements QueryInterface {

    private KnowledgeBase kb;
    private String name;
    private Hashtable qsArray = new Hashtable();
    private Hashtable drArray = new Hashtable();
    private int idGen = 0;


    //Starting
    public QueryComponent() {


    }

    private int getNewId() {
        return (++idGen);

    }

    public String getName() {
        return this.name;
    }

    @Port(name = "QueryIn")
    public void incomingQuery(Object o) {
        try {
            if (o.getClass().equals(Query.class)) {
                Query x = (Query) o;
                if (x.getDestinator().trim().equals(this.name.trim())) {
                    int id = getNewId();
                    System.out.println(this.name + " has received a new query => " + id);

                    Drop dr = new Drop();
                    QueryServantSimpleAnswers qssa = new QueryServantSimpleAnswers(this.kb, this, x, id, dr);
                    qsArray.put(id, qssa);
                    drArray.put(id, dr);
                    qssa.start();
                }
            } else if (o.getClass().equals(QueryResponse.class)) {
                QueryResponse qr = (QueryResponse) o;
                Query q = qr.getQuery();
                if (q.getOwner().trim() == this.name.trim()) {
                    System.out.println(this.name + " has received a response");
                    int id = q.getProcessId();

                    try {
                        Drop dr = (Drop) drArray.get(id);
                        dr.put(qr);
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            }

        } catch (Exception ex) {
            System.out.println(" Exception: " + ex.getMessage());
        }
    }


    @Port(name = "ConsoleIn")
    public void incomingConsole(Object o) {

        String s = (String) o;
        initiateQuery(s);
    }

    //To send queries to other components
    private void queryOutput(Object o) {
        MessagePort prodPort = getPortByName("QueryOut", MessagePort.class);
        if (prodPort != null) {
            prodPort.process(o);
        }
    }

    //To send Message to Console
    private void consoleOutput(Object o) {
        MessagePort prodPort = getPortByName("ConsoleOut", MessagePort.class);
        if (prodPort != null) {
            prodPort.process(o);
        }
    }


    public void sendQueryToPeer(Query q) {

        System.out.println(this.name + " has sent a query to " + q.getDestinator());
        MessagePort prodPort = getPortByName("QueryOut", MessagePort.class);
        if (prodPort != null) {
            prodPort.process(q);
        }
    }


    public void notifyFailure(Query q, int ID) {
        System.out.println("query Server Failed");
        killQueryServant(ID);
    }


    private void initiateQuery(String s) {
        try {
            s = s.trim();
            int id = getNewId();


            boolean sign = Literal.getSign(s);
            String[] literalString = s.split("~");


            Query qTemp = new Query(this.name, id, this.name, new Literal(((literalString.length > 1) ? literalString[1].trim() : literalString[0].trim()), "local", sign), false, new LinkedList(), new LinkedList(), new LinkedList());
            Drop dr = new Drop();
            QueryServantSimpleAnswers qssa = new QueryServantSimpleAnswers(this.kb, this, qTemp, id, dr);
            qsArray.put(id, qssa);
            drArray.put(id, dr);
            qssa.start();

        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }

    }

    public void sendResults(QueryResponse resp, int currentProcessId) {
        Query quest = resp.getQuery();
        //System .out .println("in send results");
        if (quest.getOwner().equals(this.name)) {

            String s = "query Servant responded for " + quest.getLiteral() + ": ";
            if (resp.isBooleanAnswer())
                s += resp.getReturnValue();
            //else
            //printTriadicAnswer(((MyTriadic)resp.getReturnAnswer()).getAnswerType());

            // printReceivedSet(" Supporting set: ", resp.getSupportingSet());
            //printReceivedSet(" Conflicting set: ", resp.getConflictingSet());
            consoleOutput(s);

        } else {
            queryOutput(resp);
        }

        killQueryServant(currentProcessId);

    }

    private void killQueryServant(int qsId) {
        try {
            QueryServant QS = (QueryServant) qsArray.get(qsId);
            //Kill QS to implement ????
            qsArray.remove(qsId);
        } catch (Exception ex) {
            System.out.println("Can't remove process");
        }

    }


    @Start
    public void start() {

        this.name = (String) getDictionary().get("Name");

        System.out.println("Starting " + this.name + " query component");

        try {
            String initKb = (String) getDictionary().get("InitialKnowledgeBaseFile");
            String initTrust = (String) getDictionary().get("InitialTrustFile");

            kb = new KnowledgeBase();
            kb.createNewSet(KnowledgeBase.localSetName); // set for local rules
            kb.createNewSet(KnowledgeBase.remoteSetName); // set for mapping rules

            if (initKb != null && initKb != "") {
                kb.loadKbFromFile(initKb, this.name);
                consoleOutput("Kb loaded:");
                consoleOutput(kb.getAllLiteral());
            }
            if (initTrust != null && initTrust != "") {
                kb.loadTrustOrderFromFile(initTrust);

            }
        } catch (Throwable throwable) {

            throwable.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.

        }


        getModelService().registerModelListener(new ModelListenerAdapter() {
            @Override
            public void modelUpdated() {

            }

            @Override
            public void preRollback(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void postRollback(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

    }

    @Stop
    public void stop() {
        for(Object listElement : qsArray.values() ) {
            QueryServant servant = (QueryServant)listElement;
            System.out.println("Query Servalt ID to stop: " + servant.getProcessID());
            String keyList = "";
            for(Object key : drArray.keySet()) {
                keyList += key + ",";
            }
            System.out.println("Drop List: " + keyList);
            Drop drop = (Drop)drArray.get(servant.getProcessID());
            drop.put(null);
            try {
                servant.join();
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    @Update
    public void update() {
        stop();
        start();
    }


}
