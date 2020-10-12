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
        .maxStep(500 * 1000)
        .build();

    private static DQNDenseNetworkConfiguration dqnDenseNetworkConfiguration = DQNDenseNetworkConfiguration.builder().build();

    public static void main(String[] args) throws Exception {

        MDP<GeneralState, Integer, DiscreteSpace> mdp = new GeneralMDP();

        QLearningDiscreteDense<GeneralState> dql = defineTraining(mdp);

        dql.train();

        DQNPolicy<GeneralState> pol = dql.getPolicy();

        saveForFutureReuse(pol);

        mdp.close();

    }

    private static void saveForFutureReuse(DQNPolicy<GeneralState> pol) throws IOException {
        new File("tmp/General").delete();
        pol.save("tmp/General");
    }

    private static QLearningDiscreteDense<GeneralState> defineTraining(MDP<GeneralState, Integer, DiscreteSpace> mdp) {
        return new QLearningDiscreteDense<>(mdp, dqnDenseNetworkConfiguration, qLearningConfiguration);
    }
}
