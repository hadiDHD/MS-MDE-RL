package msmderl.general;

import msmderl.data.Method;
import msmderl.data.Microservice;
import msmderl.generator.ModelGenerator;
import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.space.ArrayObservationSpace;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.ObservationSpace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GeneralMDP implements MDP<GeneralState, Integer, DiscreteSpace> {

    Microservice[] microservices;
    private Method[] methods;
    private int[] methodsInServices;
    private Microservice[] assignedMicroservice;
    private int step;
    private GeneralState curState;
    private DiscreteSpace actionSpace;
    private ObservationSpace<GeneralState> observationSpace;
    private boolean isTraining;
    private int accReward = 0;
    private int nanoentities;
    public static final int MAX_MICROSERVICE = 4;
    public static final int MAX_NANO_ENTITY = 10;
    public static final int MAX_METHOD = 25;

    private static final int NON_MATCH_PENALTY = 10;
    private static final int MATCH_REWARD = 20;
    private static final int CONTEXT_BOUND_REWARD = 20;
    private static final int CONTEXT_BOUND_PENALTY = 10;
    private static final int OVERFILL_PENALTY = 5;

    public GeneralMDP() {
        isTraining = true;
        ModelGenerator modelGenerator = new ModelGenerator(MAX_MICROSERVICE, MAX_MICROSERVICE,
                1, MAX_NANO_ENTITY,
                1, MAX_METHOD);
        this.microservices = modelGenerator.generateServiceCutterModel().getServices();
        this.methods = modelGenerator.generateMethodModel().getMethods();
        init();
    }

    public GeneralMDP(Microservice[] microservices, Method[] methods) {
        isTraining = false;
        this.microservices = microservices;
        this.methods = methods;
        init();
    }

    private void init() {
        step = 0;
        accReward = 0;
        nanoentities = nanoentitiesNum();
        this.assignedMicroservice = new Microservice[MAX_METHOD];
        this.methodsInServices = new int[MAX_MICROSERVICE];
        this.curState = new GeneralState(MAX_MICROSERVICE, microservices, methods[0], methodsInServices);
        actionSpace = new DiscreteSpace(MAX_MICROSERVICE);
        observationSpace = new ArrayObservationSpace<>(new int[]{MAX_MICROSERVICE * 3});
    }

    public void printFinalResult() {
        if (!isDone()) {
            System.out.println("Not Done!");
        }
        for (int i = 0; i < microservices.length; i++) {
            System.out.print(microservices[i].getName() + " : ");
            ArrayList<String> methodNames = new ArrayList<>();
            for (int j = 0; j < assignedMicroservice.length; j++) {
                if (assignedMicroservice[j] == microservices[i]) {
                    methodNames.add(methods[j].getName());
                }
            }
            System.out.println(methodNames);
        }
        System.out.println("AccReward :" + accReward);
    }

    @Override
    public void close() {
    }

    @Override
    public DiscreteSpace getActionSpace() {
        return actionSpace;
    }

    @Override
    public ObservationSpace<GeneralState> getObservationSpace() {
        return observationSpace;
    }

    @Override
    public boolean isDone() {
        return step >= methods.length;
    }

    @Override
    public MDP<GeneralState, Integer, DiscreteSpace> newInstance() {
        return new GeneralMDP();
    }

    @Override
    public GeneralState reset() {
        if (!isTraining) {
            return curState;
        }
        ModelGenerator modelGenerator = new ModelGenerator(MAX_MICROSERVICE, MAX_MICROSERVICE,
                1, MAX_NANO_ENTITY,
                1, MAX_METHOD);
        this.microservices = modelGenerator.generateServiceCutterModel().getServices();
        this.methods = modelGenerator.generateMethodModel().getMethods();
        init();
        return curState;
    }

    @Override
    public StepReply<GeneralState> step(Integer action) {
        int reward = 0;
        assignedMicroservice[step] = microservices[action];
        Method method = methods[step];
        Set<String> methodNE = new HashSet<>(method.getNanoentities().length);
        methodNE.addAll(Arrays.asList(method.getNanoentities()));
        for (int i = 0; i < microservices.length; i++) {
            Microservice microservice = microservices[i];
            if (i == action) {
                for (String ne : microservice.getNanoentities()) {
                    if (methodNE.contains(ne)) {
                        reward += MATCH_REWARD;
                    }
                }
            } else {
                for (String ne : microservice.getNanoentities()) {
                    if (methodNE.contains(ne)) {
                        reward -= NON_MATCH_PENALTY;
                    }
                }
            }
        }
        Microservice microservice = microservices[action];
        Set<String> serviceNE = new HashSet<>(microservice.getNanoentities().length);
        serviceNE.addAll(Arrays.asList(microservice.getNanoentities()));
        for (String ne : methodNE) {
            if (serviceNE.contains(ne)) {
                reward += CONTEXT_BOUND_REWARD;
            } else {
                reward -= CONTEXT_BOUND_PENALTY;
            }
        }
        if (methodsInServices[action] >= microservice.getNanoentities().length / nanoentities) {
            reward -= OVERFILL_PENALTY;
        }
        methodsInServices[action]++;
        step++;
        Method nextMethod;
        if (step < methods.length) {
            nextMethod = methods[step];
        } else {
            nextMethod = new Method("", new String[0]);
        }
        curState = new GeneralState(MAX_MICROSERVICE, microservices, nextMethod, methodsInServices);
        accReward += reward;
        return new StepReply<>(curState, reward, isDone(), null);
    }

    public int nanoentitiesNum() {
        HashSet<String> entites = new HashSet<>();
        for (Microservice m : microservices) {
            entites.addAll(Arrays.asList(m.getNanoentities()));
        }
        return entites.size();
    }

}
