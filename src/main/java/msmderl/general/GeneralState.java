package msmderl.general;

import msmderl.data.Method;
import msmderl.data.Microservice;
import org.deeplearning4j.rl4j.space.Encodable;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Arrays;
import java.util.HashSet;

public class GeneralState implements Encodable {
    byte[] state;

    public GeneralState(byte[] state) {
        this.state = state;
    }

    public GeneralState(int MAX_MICROSERVICE, Microservice[] microservices, Method method, int[] methodsInServices) {
        state = new byte[MAX_MICROSERVICE * 3];
        for (int i = 0; i < microservices.length; i++) {
            Microservice mS = microservices[i];
            HashSet<String> methodEntities = new HashSet<>(Arrays.asList(method.getNanoentities()));
            byte mathcing = 0;
            byte nonMatching = 0;
            for (String ne : mS.getNanoentities()) {
                if (methodEntities.contains(ne)) {
                    mathcing++;
                } else {
                    nonMatching++;
                }
            }
            state[i * 3] = mathcing;
            state[i * 3 + 1] = nonMatching;
            state[i * 3 + 2] = (byte) methodsInServices[i];
        }
        for (int i = microservices.length; i < MAX_MICROSERVICE; i++) {
            state[i * 3] = -1;
            state[i * 3 + 1] = -1;
            state[i * 3 + 2] = -1;
        }
    }

    @Override
    public double[] toArray() {
        return getData().toDoubleVector();
    }

    @Override
    public boolean isSkipped() {
        return state == null;
    }

    @Override
    public INDArray getData() {
        return Nd4j.create(state, new long[]{state.length}, DataType.INT8);
    }

    @Override
    public Encodable dup() {
        return new GeneralState(state);
    }
}
