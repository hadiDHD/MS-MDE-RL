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

    public IndividualMDP(Microservice[] microservices, Method[] methods) {
        this.microservices = microservices;
        this.methods = methods;
        init();
    }

    private void init() {
        step = 0;
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
        curState = new IndividualState(microservices, nextMethod, methodsInServices, microservices.length);
        return new StepReply<>(curState, reward, isDone(), null);
    }
}
