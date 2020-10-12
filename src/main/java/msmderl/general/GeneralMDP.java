package msmderl.general;

import msmderl.data.Method;
import msmderl.data.Microservice;
import msmderl.modelgenerator.ModelGenerator;
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
    public final int MAX_MICROSERVICE = 15;
    public final int MAX_NANO_ENTITY = 240;
    public final int MAX_METHOD = 60;

    public GeneralMDP() {
        isTraining = true;
        ModelGenerator modelGenerator = new ModelGenerator(MAX_MICROSERVICE - 3, MAX_MICROSERVICE,
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
        ModelGenerator modelGenerator = new ModelGenerator(MAX_MICROSERVICE - 3, MAX_MICROSERVICE,
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
                        reward += 20;
                    }
                }
            } else {
                for (String ne : microservice.getNanoentities()) {
                    if (methodNE.contains(ne)) {
                        reward -= 10;
                    }
                }
            }
        }
        Microservice microservice = microservices[action];
        Set<String> serviceNE = new HashSet<>(microservice.getNanoentities().length);
        serviceNE.addAll(Arrays.asList(microservice.getNanoentities()));
        for (String ne : methodNE) {
            if (serviceNE.contains(ne)) {
                reward += 20;
            } else {
                reward -= 10;
            }
        }
        if (methodsInServices[action] >= methods.length / microservices.length) {
            reward -= 5;
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
        return new StepReply<>(curState, reward, isDone(), null);
    }

}
