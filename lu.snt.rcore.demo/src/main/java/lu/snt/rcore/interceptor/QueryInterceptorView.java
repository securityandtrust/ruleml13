package lu.snt.rcore.interceptor;

/**
 * Created with IntelliJ IDEA.
 * User: assaad.moawad
 * Date: 15/05/13
 * Time: 14:25
 * To change this template use File | Settings | File Templates.
 */

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import javax.swing.*;
import java.awt.*;

public class QueryInterceptorView extends JPanel {


    private JButton btnNext;
    private JButton btnReset;
    private JPanel buttonsPanel;
    private JPanel graphPanel;
    private JSplitPane splitPane;
    private JCheckBox checkBox;
    private mxGraph graph;
    private mxGraphComponent gComp;


    public static final int NB_LINES = 10;
    public static final int NB_COLS = 1;
    public static final int BUTTON_SPACE = 5;


    public QueryInterceptorView() {
        super(new BorderLayout());

        buttonsPanel = new JPanel();
        graphPanel = new JPanel(new GridLayout(1, 1, 1, 1));

        //Button Panel layout
        buttonsPanel = new JPanel(new GridLayout(NB_LINES, NB_COLS, BUTTON_SPACE, BUTTON_SPACE));

        checkBox = new JCheckBox("Demo Mode", true);
        buttonsPanel.add(checkBox);
        buttonsPanel.add(checkBox, BorderLayout.CENTER);

        btnNext = new JButton("Next");
        buttonsPanel.add(btnNext);
        buttonsPanel.add(btnNext, BorderLayout.CENTER);

        btnReset = new JButton("Reset");
        buttonsPanel.add(btnReset);
        buttonsPanel.add(btnReset, BorderLayout.CENTER);


        //Create a split pane with the two scroll panes in it.
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphPanel, buttonsPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(650);

        //Provide minimum sizes for the two components in the split pane.
        Dimension minimumSize = new Dimension(650, 600);
        graphPanel.setMinimumSize(minimumSize);

        minimumSize = new Dimension(130, 600);
        buttonsPanel.setMinimumSize(minimumSize);


        //Provide a preferred size for the split pane.
        this.setPreferredSize(new Dimension(850, 600));
        //splitPane.setPreferredSize(new Dimension(830, 600));

        add(splitPane, BorderLayout.CENTER);


    }

    public void setGraph(mxGraph g) {
        graph = g;
        gComp = new mxGraphComponent(graph);
        gComp.setSize(new Dimension(600, 500));

        graphPanel.removeAll();
        graphPanel.add(gComp, BorderLayout.CENTER);

    }

    public void updateGraph() {
        if (gComp != null && graph != null) {
            gComp.revalidate();
            gComp.repaint();
        }
        graphPanel.revalidate();
        graphPanel.repaint();
    }

    public JButton getBtnNext() {
        return btnNext;
    }


    public JButton getBtnReset() {
        return btnReset;
    }

    public JCheckBox getcheckBox() {
        return checkBox;
    }

    /*public void setGraph(mxGraph graph)
    {
     //   this.graph=graph;
        //Update view here
    }
      */
}
