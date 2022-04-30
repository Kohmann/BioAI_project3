import kotlin.system.measureTimeMillis


class GeneticAlgorithm(private val image: ImageObject) {

    // PARAMETERS
    var params = Parameters()

    private val populationSize: Int = params.populationSize
    private val mutationRate: Double = params.mutationRate
    private val crossoverRate: Double = params.crossoverRate
    private val numGenerations: Int = params.numGenerations


    val population = Population(populationSize, image)
    private var generation = 0

    fun runNSGA() {

        repeat(numGenerations) {
            println("Generation: $generation")
            val start = System.currentTimeMillis()

            population.combineWithOffspring() // combines parents with offspring

            // Non Dominated Sorting

            population.evaluate()

            println("\tUnique individuals ${population.individuals.map { it.hashCode() }.toSet().size} of ${population.individuals.size}")

            val segmentCount = population.individuals.map { it.segments.flatten().sum() }
            val count = image.getHeight() * image.getWidth() -1
            //val sum = count * (count + 1) / 2
            //var bugSegment: Individual
            //if (segmentCount.any { it != sum })
            //    population.individuals[segmentCount.indexOfFirst { it != sum }].printInfo()

            println("\tThe segments includes all pixels just once: ${segmentCount.all { it == segmentCount[0]}}")
            println("\tThe segments sums: ${segmentCount.joinToString(", ")}")
            val segmentSizes = population.individuals.map { it.segments.size }
            println("\tSegment number: ${segmentSizes.sum()}, average segment size: ${segmentSizes.average()}")

            population.assignRank()
            population.selection() // finds all parent candidates

            population.createOffspring(mutationRate, crossoverRate)

            val end = System.currentTimeMillis()
            println("\tEvaluation: Time used in ${end - start}ms, ${(end - start) / 1000}s")

            generation++
        }

        population.combineWithOffspring()
        population.evaluate()
        population.assignRank()
        population.stopThreads()
        println("Best connectivity individuals:")
        //population.fronts[0].forEach {
        //    println("\t${it.segments.size}")
        //    image.save(it, mode="black") // saving as image, black or green
        //    image.save(it, mode="green") // saving as image, black or green
        //}
        println("Connectivity")
        population.fronts[0].forEach {
            print("\t, ${it.connectivity}")
        }
        println("\nEdge")
        population.fronts[0].forEach {
            print("\t, ${it.edgeValue}")
        }
        println("\nOverall")
        population.fronts[0].forEach {
            print("\t, ${it.overallDeviation}")
        }

    }

    fun runGA() {
        var prevBestFitness = 0.0
        repeat(numGenerations) {
            println("Generation: $generation")

            population.combineWithOffspring() // combines parents with offspring
            population.evaluate()
            val generationBest = population.bestIndividual().weightedFitness
            print("\tCurrent best: %-10.2f  Fitness improvement: %-10.2f".format(generationBest, (prevBestFitness - generationBest) ))
            println(", Segments: ${population.bestIndividual().segments.size}")

            if (generation % 5 == 0)
                image.save(population.bestIndividual(), mode="green", extra_info = "_generation=%d".format(generation)) // saving as image, black or green

            prevBestFitness = generationBest
            population.selectionGA() // finds all parent candidates
            population.createOffspring(mutationRate, crossoverRate)

            generation++
        }
        population.combineWithOffspring()
        population.evaluate()
        population.stopThreads()
        val best = population.bestIndividual()
        println("Best individual:")
        best.printInfo()
        image.save(best, mode="green", extra_info = "_final") // saving as image, black or green

    }

}
