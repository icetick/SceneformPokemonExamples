package alex.orobinsk.sceneformarcoreexamples

class ModelProvider {
    private fun provideEvolveMatrix() = arrayListOf(
        mutableListOf(R.raw.bulbasaur, R.raw.ivysaur, R.raw.venusaur, R.raw.venusaur_mega),
        mutableListOf(R.raw.charmander, R.raw.charmeleon, R.raw.charizard, R.raw.charizard_mega),
        mutableListOf(R.raw.squirtle, R.raw.wartortle, R.raw.blastoise, R.raw.blastoise_mega)
    )

    fun provideCommonRenderables() = listOf(R.raw.beedrill, R.raw.pikachu, R.raw.bulbasaur, R.raw.charmander, R.raw.squirtle)

    fun provideNextEvo(currentResource: Int?): Int {
        val evolveMatrix = provideEvolveMatrix()
        try {
            for (item in evolveMatrix) {
                if (item.contains(currentResource)) {
                    return item[item.indexOf(currentResource) + 1]
                }
            }
        } catch (ex: Exception) {
            for (item in evolveMatrix) {

                if (item.contains(currentResource)) {
                    return item[0]
                }

            }
        }
        return 0
    }
}