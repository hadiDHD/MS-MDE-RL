package msmderl.gui;//Generated by GuiGenie - Copyright (c) 2004 Mario Awad.
//Home Page http://guigenie.cjb.net - Check often for new versions!

import javax.swing.*;
import java.awt.*;

public class GUI extends JPanel {
    private JTabbedPane jcomp1;

    public GUI() {
        //construct components
        jcomp1 = new JTabbedPane();
        jcomp1.add("Solver", new Solver());
        jcomp1.add("General Trainer", new General());
        jcomp1.add("Individual Trainer", new Individual());

        //adjust size and set layout
        setPreferredSize (new Dimension (752, 431));
        setLayout (null);

        //add components
        add (jcomp1);

        //set component bounds (only needed by Absolute Positioning)
        jcomp1.setBounds (0, 0, 752, 431);
    }


    public static void main (String[] args) {
        JFrame frame = new JFrame ("MicroMapper");
        frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add (new GUI());
        frame.pack();
        frame.setVisible (true);
    }
}