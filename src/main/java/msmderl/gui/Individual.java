package msmderl.gui;//Generated by GuiGenie - Copyright (c) 2004 Mario Awad.
//Home Page http://guigenie.cjb.net - Check often for new versions!

import com.google.gson.Gson;
import msmderl.data.MethodModel;
import msmderl.data.MicroserviceModel;
import msmderl.individual.IndividualMDP;
import msmderl.individual.IndividualState;
import org.deeplearning4j.rl4j.learning.configuration.QLearningConfiguration;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteDense;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.network.configuration.DQNDenseNetworkConfiguration;
import org.deeplearning4j.rl4j.policy.DQNPolicy;
import org.deeplearning4j.rl4j.space.DiscreteSpace;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

public class Individual extends JPanel {
    private JLabel jcomp1;
    private JTextField microModel;
    private JLabel jcomp3;
    private JTextField episode;
    private JLabel jcomp5;
    private JTextField reward;
    private JLabel jcomp7;
    private JTextField gamma;
    private JButton trainBT;
    private JButton microBT;
    private JLabel jcomp11;
    private JTextField methodModel;
    private JButton methodBT;
    private JLabel jcomp14;
    private JCheckBox useTransfer;
    private JTextField transferModel;
    private JButton transferBT;
    private JLabel status;

    private File MS;
    private File methods;
    private File transfer;

    public Individual() {
        //construct components
        jcomp1 = new JLabel("Microservices Model: ");
        microModel = new JTextField(5);
        jcomp3 = new JLabel("Episode Number: ");
        episode = new JTextField(5);
        jcomp5 = new JLabel("Reward Factor: ");
        reward = new JTextField(5);
        jcomp7 = new JLabel("Gamma: ");
        gamma = new JTextField(5);
        trainBT = new JButton("Train");
        microBT = new JButton("Browse");
        jcomp11 = new JLabel("Methods Model: ");
        methodModel = new JTextField(5);
        methodBT = new JButton("Browse");
        jcomp14 = new JLabel("Transfer Learning From: ");
        useTransfer = new JCheckBox("Use Transfer Learning");
        transferModel = new JTextField(5);
        transferBT = new JButton("Browse");
        status = new JLabel("");

        //set components properties
        microModel.setEnabled(false);
        methodModel.setEnabled(false);
        transferModel.setEnabled(false);
        reward.setText("0.9");
        gamma.setText("0.1");
        microBT.addActionListener(e -> {
            MS = open(true);
            microModel.setText(MS.getName());
        });
        methodBT.addActionListener(e -> {
            methods = open(true);
            methodModel.setText(methods.getName());
        });
        transferBT.addActionListener(e -> {
            transfer = open(false);
            transferModel.setText(transfer.getName());
        });
        jcomp14.setVisible(false);
        transferModel.setVisible(false);
        transferBT.setVisible(false);
        useTransfer.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    jcomp14.setVisible(true);
                    transferModel.setVisible(true);
                    transferBT.setVisible(true);
                } else {
                    jcomp14.setVisible(false);
                    transferModel.setVisible(false);
                    transferBT.setVisible(false);
                }
            }
        });

        trainBT.addActionListener(e -> {
            status.setText("Please Wait...");
            train();
            status.setText("Done");
        });

        //adjust size and set layout
        setPreferredSize(new Dimension(752, 431));
        setLayout(null);

        //add components
        add(jcomp1);
        add(microModel);
        add(jcomp3);
        add(episode);
        add(jcomp5);
        add(reward);
        add(jcomp7);
        add(gamma);
        add(trainBT);
        add(microBT);
        add(jcomp11);
        add(methodModel);
        add(methodBT);
        add(jcomp14);
        add(useTransfer);
        add(transferModel);
        add(transferBT);
        add(status);

        //set component bounds (only needed by Absolute Positioning)
        jcomp1.setBounds(20, 15, 190, 25);
        microModel.setBounds(215, 15, 370, 25);
        jcomp3.setBounds(20, 195, 185, 25);
        episode.setBounds(215, 195, 155, 25);
        jcomp5.setBounds(20, 105, 185, 25);
        reward.setBounds(215, 105, 155, 25);
        jcomp7.setBounds(20, 150, 185, 25);
        gamma.setBounds(215, 150, 155, 25);
        trainBT.setBounds (340, 325, 100, 25);
        microBT.setBounds(615, 15, 100, 25);
        jcomp11.setBounds(20, 55, 185, 25);
        methodModel.setBounds(215, 55, 370, 25);
        methodBT.setBounds(615, 55, 100, 25);
        jcomp14.setBounds(15, 285, 185, 25);
        useTransfer.setBounds(15, 245, 185, 25);
        transferModel.setBounds(215, 285, 365, 25);
        transferBT.setBounds (610, 285, 100, 25);
        status.setBounds (340, 365, 100, 25);
    }

    private void train() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showSaveDialog(null);
            File file = fileChooser.getSelectedFile();
            QLearningConfiguration QL =
                    QLearningConfiguration.builder()
                            .maxEpochStep(Integer.MAX_VALUE)
                            .maxStep(Integer.parseInt(episode.getText()))
                            .gamma(Double.parseDouble(gamma.getText()))
                            .rewardFactor(Double.parseDouble(reward.getText()))
                            .build();
            Gson gson = new Gson();
            MicroserviceModel serviceModel = gson.fromJson(readLineByLineJava8(MS.getAbsolutePath()), MicroserviceModel.class);

            MethodModel methodModel = gson.fromJson(readLineByLineJava8(methods.getAbsolutePath()), MethodModel.class);

            MDP<IndividualState, Integer, DiscreteSpace> mdp = new IndividualMDP(serviceModel.getServices(), methodModel.getMethods());

            QLearningDiscreteDense<IndividualState> dql;
            if (useTransfer.isSelected()) {
                dql = new QLearningDiscreteDense<>(mdp, loadPreviousAgent().getNeuralNet(), QL);
            } else {
                dql = new QLearningDiscreteDense<>(mdp, DQNDenseNetworkConfiguration.builder().build(), QL);
            }
            dql.train();

            DQNPolicy<IndividualState> pol = dql.getPolicy();

            saveForFutureReuse(pol, file);

            mdp.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void saveForFutureReuse(DQNPolicy<IndividualState> pol, File file) throws IOException {
        file.mkdirs();
        file.delete();
        pol.save(file.getAbsolutePath());
    }

    private File open(boolean json) {
        JFileChooser fileChooser = new JFileChooser();
        if (json) {
            fileChooser.addChoosableFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.getName().toLowerCase().endsWith(".json");
                }

                @Override
                public String getDescription() {
                    return "json";
                }
            });
        }
        fileChooser.showOpenDialog(null);
        return fileChooser.getSelectedFile();
    }

    private String readLineByLineJava8(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    private DQNPolicy<IndividualState> loadPreviousAgent() throws IOException {
        System.out.println(transfer.getAbsolutePath());
        return DQNPolicy.load(transfer.getAbsolutePath());
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Individual");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new Individual());
        frame.pack();
        frame.setVisible(true);
    }
}
