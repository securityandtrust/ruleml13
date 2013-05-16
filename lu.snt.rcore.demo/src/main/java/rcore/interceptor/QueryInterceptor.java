package rcore.interceptor;

/**
 * Created with IntelliJ IDEA.
 * User: assaad.moawad
 * Date: 15/05/13
 * Time: 14:28
 * To change this template use File | Settings | File Templates.
 */

import org.kevoree.annotation.*;
import org.kevoree.framework.MessagePort;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Provides({

        @ProvidedPort(name = "QueryIn", type = PortType.MESSAGE)
})

@Requires({
        @RequiredPort(name = "QueryOut", type = PortType.MESSAGE, optional = true)
})


//((MessagePort)getPortByName("QueryOut")).process(object data);

@ComponentType
@Library(name = "Serval_RN12")
public class QueryInterceptor extends org.kevoree.framework.AbstractComponentType implements QueryInterceptorInterface {


    private QueryInterceptorView view;
    private QueryInterceptorModel model;
    private ActionListener actionListener;

    public void init() {

        //Set the view
        view = new QueryInterceptorView();
        JFrame window = new JFrame("interceptor");
        window.add(view, BorderLayout.CENTER);
        window.setSize(750, 600);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(window.getParent());
        window.setVisible(true);

        //Set the Model parameters
        model = new QueryInterceptorModel();
        model.setQueryInterceptorInterface(this);
        model.init();
        model.setDemoMode(view.getcheckBox().isSelected());


        //Link view to model
        actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                model.nextStep();
            }
        };
        view.getBtnNext().addActionListener(actionListener);

        actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                model.reset();
                view.setGraph(model.getGraph());
            }
        };
        view.getBtnReset().addActionListener(actionListener);

        actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                model.setDemoMode(view.getcheckBox().isSelected());
            }
        };
        view.getcheckBox().addActionListener(actionListener);

        view.setGraph(model.getGraph());


    }

    public void updateGraph() {
        view.updateGraph();
    }


    @Port(name = "QueryIn")
    public void incomingQuery(Object o) {
        try {
            model.addQuery(o);

            //process change to implement

        } catch (Exception ex) {
            System.out.println(" Exception: " + ex.getMessage());
        }
    }


    //Method to be used to send the queries back to components
    public void queryOutput(Object o) {
        MessagePort prodPort = getPortByName("QueryOut", MessagePort.class);
        if (prodPort != null) {
            prodPort.process(o);
        }
    }


    @Start
    public void start() {
        init();

    }

    @Stop
    public void stop() {

    }


}


