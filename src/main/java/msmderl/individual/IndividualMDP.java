package msmderl.individual;

import msmderl.data.Method;
import msmderl.data.Microservice;
import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.space.ArrayObservationSpace;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.ObservationSpace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class IndividualMDP implements MDP<IndividualState, Integer, DiscreteSpace> {

    Microservice[] microservices;
    private Method[] methods;
    private int[] methodsInServices;
    private Microservice[] assignedMicroservice;
    private int step;
    private IndividualState curState;
    private DiscreteSpace actionSpace;
    private ObservationSpace<IndividualState> observationSpace;
    private int nanoentities;

    private static final int NON_MATCH_PENALTY = 10;
    private static final int MATCH_REWARD = 20;
    private static final int CONTEXT_BOUND_REWARD = 20;
    private static final int CONTEXT_BOUND_PENALTY = 10;
    private static final int OVERFILL_PENALTY = 5;

    public IndividualMDP(Microservice[] microservices, Method[] methods) {
        this.microservices = microservices;
        this.methods = methods;
        init();
    }

    private void init() {
        step = 0;
        nanoentities = nanoentitiesNum();
        this.assignedMicroservice = new Microservice[methods.length];
        this.methodsInServices = new int[microservices.length];
        this.curState = new IndividualState(microservices, methods[0], methodsInServices, microservices.length);
        actionSpace = new DiscreteSpace(microservices.length);
        observationSpace = new ArrayObservationSpace<>(new int[]{microservices.length * 3});
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
    public ObservationSpace<IndividualState> getObservationSpace() {
        return observationSpace;
    }

    @Override
    public boolean isDone() {
        return step >= methods.length;
    }

    @Override
    public MDP<IndividualState, Integer, DiscreteSpace> newInstance() {
        return new IndividualMDP(microservices, methods);
    }

    @Override
    public IndividualState reset() {
        init();
        return curState;
    }

    @Override
    public StepReply<IndividualState> step(Integer action) {
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
        curState = new IndividualState(microservices, nextMethod, methodsInServices, microservices.length);
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
