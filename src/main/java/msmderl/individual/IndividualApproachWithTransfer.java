package msmderl.individual;

import com.google.gson.Gson;
import msmderl.data.Method;
import msmderl.data.MethodModel;
import msmderl.data.Microservice;
import msmderl.data.MicroserviceModel;
import org.deeplearning4j.rl4j.learning.configuration.QLearningConfiguration;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteDense;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.policy.DQNPolicy;
import org.deeplearning4j.rl4j.space.DiscreteSpace;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;


public class IndividualApproachWithTransfer {

    private static QLearningConfiguration QL =
        QLearningConfiguration.builder()
            .maxEpochStep(Integer.MAX_VALUE)
            .maxStep(2 * 1000)
            .gamma(0.1)
            .rewardFactor(0.9)
            .build();

    public static void main(String[] args) throws Exception {

        Gson gson = new Gson();

        MicroserviceModel serviceModel = gson.fromJson(readJsonFile(), MicroserviceModel.class);

        MethodModel methodModel = gson.fromJson(readJsonFile(), MethodModel.class);

        train(serviceModel.getServices(), methodModel.getMethods());

        solveRealProblem(serviceModel.getServices(), methodModel.getMethods());
    }

    private static void train(Microservice[] microservices, Method[] methods) throws Exception {

        MDP<IndividualState, Integer, DiscreteSpace> mdp = new IndividualMDP(microservices, methods);

        QLearningDiscreteDense<IndividualState> dql = defineTraining(mdp, loadGeneralAgent(microservices.length));

        dql.train();

        DQNPolicy<IndividualState> pol = dql.getPolicy();

        saveForFutureReuse(pol);

        mdp.close();

    }

    private static String readJsonFile() throws FileNotFoundException {
        JFileChooser fileChooser = new JFileChooser();
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
        fileChooser.showOpenDialog(null);
        return readLineByLineJava8(fileChooser.getSelectedFile().getAbsolutePath());
    }

    private static String readLineByLineJava8(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    private static void saveForFutureReuse(DQNPolicy<IndividualState> pol) throws IOException {
        new File("trained/Individual").delete();
        pol.save("trained/Individual");
    }

    private static QLearningDiscreteDense<IndividualState> defineTraining(MDP<IndividualState, Integer, DiscreteSpace> mdp, DQNPolicy<IndividualState> pol) {
        return new QLearningDiscreteDense<>(mdp, pol.getNeuralNet(), QL);
    }


    private static void solveRealProblem(Microservice[] microservices, Method[] methods) throws Exception {
        IndividualMDP mdp2 = new IndividualMDP(microservices, methods);

        DQNPolicy<IndividualState> pol2 = loadPreviousAgent();

        playByStep(mdp2, pol2);

    }

    public static void playByStep(IndividualMDP mdp, DQNPolicy<IndividualState> policy) throws IOException {
        policy.play(mdp);
        mdp.printFinalResult();
    }

    private static DQNPolicy<IndividualState> loadPreviousAgent() throws IOException {
        System.out.println(new File("trained/Individual").getAbsolutePath());
        return DQNPolicy.load("trained/Individual");
    }

    private static DQNPolicy<IndividualState> loadGeneralAgent(int msNum) throws IOException {
        System.out.println(new File("trained/General " + msNum).getAbsolutePath());
        return DQNPolicy.load("trained/General " + msNum);
    }
}
