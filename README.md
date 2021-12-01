# MS-MDE-RL
This project aims to facilitate migration from monolithic software architectures to Microservices by using Reinforcement Learning and Model Driven Engineering.
## Setup
This project can be cloned as a Maven Java Project. All the dependencies are present in the pom file.
## Using
### General Approach
This project comes with pretrained NNETs (neural networks) to map methods to microservices for software systems with up to 9 microservice. You can train your own neural networks for larger systems as well. Each of these pretrained NNETs are trained for 10,000 episodes.
#### Running Pretrained NNETs
By executing the main method of the GeneralSolver.java, the program will ask for the services model in JSON format and then the methods model also in JSON format. Then it will find the pretrained NNET with appropriate number of microservices and generates the mapping of methods to microservices. The output will be shown in the console.
#### Training new NNETs
By executing the main method of the GeneralTrainer.java, a new NNET will be trained.
The number of training episodes, reward factor and gamma can be defined here.

`private static QLearningConfiguration qLearningConfiguration = QLearningConfiguration.builder()`

`.maxEpochStep(Integer.MAX_VALUE)`

`.maxStep(10 * 1000)`

`.rewardFactor(0.9)`

`.gamma(0.1)`

`.build();`

The number of microservices should be defined in the GeneralMDP.java file as the MAX_MICROSERVICE attribute.

`public static final int MAX_MICROSERVICE = 9;`

After the training, the NNET can be used as other pretrained NNETs.

### Individual Approach
In the individual approach, for each software system, we train and then use a new NNET.
#### Training from scratch
The IndividualApproach.java class works like the GeneralTrainer.java and GeneralSolver.java classes put together, so can change the training configuration as you wish like the GeneralTrainer, and you will be prompted for the microservices and the methods model like the GeneralSolver.
#### Transfer Learning from General Approach
By using the IndividualApproachWithTransfer.java it is possible to load a General NNET as the starting point of training for the Individual Approach, given that there exist a pretrained General NNET with the same number of microservices.