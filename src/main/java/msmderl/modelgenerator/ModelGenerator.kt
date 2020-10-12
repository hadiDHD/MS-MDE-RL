package msmderl.modelgenerator

import msmderl.data.Method
import msmderl.data.MethodModel
import msmderl.data.Microservice
import msmderl.data.MicroserviceModel
import kotlin.random.Random

class ModelGenerator(minService: Int, maxService: Int, minNanoEntity: Int, maxNanoEntity: Int, minMethod: Int, maxMethod: Int) {

    val rand = Random(System.currentTimeMillis())
    var noOfServices: Int
    var noOfNanoEntities: Int

    var noOfMethods: Int
    var minNoOfNEPerMethod: Int
    var maxNoOfNEPerMethod: Int

    var microservices: MutableList<KotlinMicroservice>

    var methods: MutableList<Method>

    init {
        noOfServices = rand.nextInt(minService, maxService + 1)
        noOfNanoEntities = rand.nextInt(minNanoEntity, maxNanoEntity + 1)
        noOfMethods = rand.nextInt(minMethod, maxMethod + 1)
        minNoOfNEPerMethod = 1
        maxNoOfNEPerMethod = noOfNanoEntities
        microservices = mutableListOf()
        methods = mutableListOf()
    }

    fun generateMethodModel(): MethodModel {
        for (i in 1..noOfMethods) {
            val name = "M $i"
            val entities = mutableSetOf<String>()
            val noOfEntities = rand.nextInt(minNoOfNEPerMethod, maxNoOfNEPerMethod + 1)
            while (entities.size < noOfEntities) {
                val chosenEntity = rand.nextInt(1, noOfNanoEntities + 1)
                entities.add("NE $chosenEntity")
            }
            methods.add(Method(name, entities.toTypedArray()))
        }
        return MethodModel(methods.toTypedArray())
    }

    fun generateServiceCutterModel(): MicroserviceModel {
        for (i in 0 until noOfServices) {
            microservices.add(KotlinMicroservice((i + 1).toString(), "MS " + ('A' + i).toString(), mutableListOf()))
        }
        for (i in 1..noOfNanoEntities) {
            val chosenMSIndex = rand.nextInt(0, noOfServices)
            microservices[chosenMSIndex].nanoentities.add("NE $i")
        }
        return MicroserviceModel(microservices.map { Microservice(it.id, it.name, it.nanoentities.toTypedArray()) }.toTypedArray())
    }
}

