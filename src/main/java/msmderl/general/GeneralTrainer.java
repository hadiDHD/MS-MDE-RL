package msmderl.general;

import org.deeplearning4j.rl4j.learning.configuration.QLearningConfiguration;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteDense;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.network.configuration.DQNDenseNetworkConfiguration;
import org.deeplearning4j.rl4j.policy.DQNPolicy;
import org.deeplearning4j.rl4j.space.DiscreteSpace;

import java.io.File;
import java.io.IOException;


public class GeneralTrainer {

    private static QLearningConfiguration qLearningConfiguration = QLearningConfiguration.builder()
            .maxEpochStep(Integer.MAX_VALUE)
            .maxStep(10 * 1000)
            .rewardFactor(0.9)
            .gamma(0.1)
            .build();

    public static DQNDenseNetworkConfiguration dqnDenseNetworkConfiguration = DQNDenseNetworkConfiguration.builder().build();

    public static void main(String[] args) throws Exception {

        GeneralMDP mdp = new GeneralMDP();

        QLearningDiscreteDense<GeneralState> dql = defineTraining(mdp);

        dql.train();

        DQNPolicy<GeneralState> pol = dql.getPolicy();

        saveForFutureReuse(pol, mdp.MAX_MICROSERVICE);

        mdp.close();

    }

    private static void saveForFutureReuse(DQNPolicy<GeneralState> pol, int MAX_MICROSERVICE) throws IOException {
        File file = new File("trained/General " + MAX_MICROSERVICE);
        file.mkdirs();
        file.delete();
        pol.save("trained/General " + MAX_MICROSERVICE);
    }

    private static QLearningDiscreteDense<GeneralState> defineTraining(MDP<GeneralState, Integer, DiscreteSpace> mdp) {
        return new QLearningDiscreteDense<>(mdp, dqnDenseNetworkConfiguration, qLearningConfiguration);
    }
}
