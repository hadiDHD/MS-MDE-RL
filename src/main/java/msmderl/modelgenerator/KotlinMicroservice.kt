package msmderl.modelgenerator

data class KotlinMicroservice(
    val id: String,
    val name: String,
    val nanoentities: MutableList<String>
)
