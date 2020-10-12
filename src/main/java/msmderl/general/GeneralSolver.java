package msmderl.general;

import com.google.gson.Gson;
import msmderl.data.Method;
import msmderl.data.MethodModel;
import msmderl.data.Microservice;
import msmderl.data.MicroserviceModel;
import org.deeplearning4j.rl4j.policy.DQNPolicy;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class GeneralSolver {
    public static void main(String[] args) throws Exception {

        Gson gson = new Gson();

        MicroserviceModel serviceModel = gson.fromJson(readJsonFile(), MicroserviceModel.class);

        MethodModel methodModel = gson.fromJson(readJsonFile(), MethodModel.class);

        solveRealProblem(serviceModel.getServices(), methodModel.getMethods());
    }

    private static String readJsonFile() {
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


    private static void solveRealProblem(Microservice[] microservices, Method[] methods) throws Exception {
        GeneralMDP mdp2 = new GeneralMDP(microservices, methods);

        DQNPolicy<GeneralState> pol2 = loadPreviousAgent();

        playByStep(mdp2, pol2);
    }

    public static void playByStep(GeneralMDP mdp, DQNPolicy<GeneralState> policy) throws IOException {
        policy.play(mdp);
        mdp.printFinalResult();
    }

    private static DQNPolicy<GeneralState> loadPreviousAgent() throws IOException {
        System.out.println(new File("tmp/General").getAbsolutePath());
        return DQNPolicy.load("tmp/General");
    }
}
