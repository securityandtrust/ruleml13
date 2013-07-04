package lu.snt.rcore.interceptor;

/**
 * Created with IntelliJ IDEA.
 * User: assaad.moawad
 * Date: 15/05/13
 * Time: 14:26
 * To change this template use File | Settings | File Templates.
 */

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxEdgeLabelLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;
import lu.snt.rcore.agencies.Query;
import lu.snt.rcore.agencies.QueryResponse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class QueryInterceptorModel {
    private boolean demoMode;
    private boolean removeDone;   //to implement
    // private mxGraph graph;
    private List<Object> queriesListened;
    private QueryInterceptorInterface intComp;

    private mxGraph graph;
    private Object parent;

    private List<QueryResponse> queriesResponse; //To implement

    private int edgeCount;

    private final int CENTER_X = 250;
    private final int CENTER_Y = 250;
    private final int RADIUS = 200;
    private final double LEN = 5;
    private final double WID = 5;


    public void init() {
        //  graph = new mxGraph();
        queriesListened = new ArrayList<Object>();
        graph = new mxGraph();
        graph.setMultigraph(true);
        graph.setAutoSizeCells(true);

               parent = graph.getDefaultParent();
        edgeCount = 0;
        intComp.updateGraph();
    }

    public void setQueryInterceptorInterface(QueryInterceptorInterface intComp) {
        this.intComp = intComp;
    }


    public void reset() {
        init();

    }


    public void setDemoMode(boolean newVal) {
        demoMode = newVal;
        if (demoMode == false && queriesListened.isEmpty() == false)
            nextStep();
    }

    private mxCell containVertex(String id) {
        Object[] ss = graph.getChildVertices(parent);


        for (int i = 0; i < ss.length; i++) {
            mxCell temp = (mxCell) ss[i];

            if (temp.getId().toString().equals(id))
                return temp;
        }

        return null;
    }

    private mxCell newVertex(String id) {
        mxCell temp = (mxCell) graph.insertVertex(parent, id, id, 100, 10, LEN, WID, "resizable=1;autosize=1;");
        return temp;
    }

    public void addQuery(Object o) {
        queriesListened.add(o);
        while (demoMode == false && queriesListened.isEmpty() == false) {
            nextStep();
        }

        String sender, receiver, literal;

        graph.getModel().beginUpdate();

        if (o.getClass().equals(Query.class)) {
            Query x = (Query) o;
            sender = x.getOwner();
            receiver = x.getDestinator();
            literal = x.getLiteral().getSignWithName();

            mxCell cellSender, cellReceiver;

            cellSender = containVertex(sender);
            if (cellSender == null) {
                cellSender = newVertex(sender);
            }

            cellReceiver = containVertex(receiver);
            if (cellReceiver == null) {
                cellReceiver = newVertex(receiver);
            }

            cellSender.setValue(cellSender.getValue() + "\n" + literal + "=?");
            edgeCount++;
            mxCell edge = (mxCell) graph.insertEdge(parent, null, edgeCount+"- "+literal+"=?", cellSender, cellReceiver);
            edge.getGeometry().setRelative(true);
            edge.getGeometry().setX(0.2);
            edge.getGeometry().setY(0.2);

            graph.updateCellSize(cellSender);
            graph.updateCellSize(cellReceiver);


        } else if (o.getClass().equals(QueryResponse.class)) {
            QueryResponse qr = (QueryResponse) o;
            Query q = qr.getQuery();

            receiver = q.getOwner();
            sender = q.getDestinator();
            literal = q.getLiteral().getSignWithName();
            String response;

            String color;
            if (qr.getReturnValue())
            {
                response = "true";
                color ="green";
            }
            else     {
                response = "false";
                color ="red";
            }

            mxCell cellSender, cellReceiver;

            cellSender = containVertex(sender);
            if (cellSender == null) {
                cellSender = newVertex(sender);
            }

            cellReceiver = containVertex(receiver);
            if (cellReceiver == null) {
                cellReceiver = newVertex(receiver);
            }


            String updateAnswer = cellReceiver.getValue().toString();
            updateAnswer=updateAnswer.replaceAll(literal+"=\\?",literal+"="+response);
            cellReceiver.setValue(updateAnswer);




            edgeCount++;
            mxCell edge = (mxCell) graph.insertEdge(parent, null, edgeCount+"- "+ response, cellSender, cellReceiver, "strokeColor="+color+";fontColor="+color+";");

            edge.getGeometry().setRelative(true);
            edge.getGeometry().setX(0.3);
            edge.getGeometry().setY(0.3);


            graph.updateCellSize(cellSender);
            graph.updateCellSize(cellReceiver);


        }
        if (graph != null) {

          /*  mxCircleLayout layout2 = new mxCircleLayout(graph);
            layout2.setRadius(RADIUS);
            layout2.setX0(CENTER_X);
            layout2.setY0(CENTER_Y);
            layout2.execute(graph.getDefaultParent());              */


            mxCompactTreeLayout layout = new mxCompactTreeLayout(graph);
            layout.setHorizontal(false);
            layout.setLevelDistance(70);
            layout.setNodeDistance(60);

            layout.execute(graph.getDefaultParent());

            mxParallelEdgeLayout layout2 = new mxParallelEdgeLayout(graph);

            layout2.execute(graph.getDefaultParent());

            mxEdgeLabelLayout layout3 = new mxEdgeLabelLayout(graph);
            layout3.execute(graph.getDefaultParent());
            graph.getModel().endUpdate();
        }


        intComp.updateGraph();

    }

    public mxGraph getGraph() {
        return graph;
    }

    public void nextStep() {

        try {
            List l2 = new ArrayList(queriesListened);
            Object temp;

            Iterator<Object> iterator = l2.iterator();
            while (iterator.hasNext()) {
                temp = iterator.next();
                intComp.queryOutput(temp);
                queriesListened.remove(temp);

            }
            while (demoMode == false && queriesListened.isEmpty() == false) {
                nextStep();
            }
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }

    }


}
