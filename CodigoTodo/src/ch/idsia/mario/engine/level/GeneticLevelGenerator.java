package ch.idsia.mario.engine.level;

import ch.idsia.mario.engine.sprites.Enemy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by roka9 on 01/12/2015.
 */
public class GeneticLevelGenerator {

    // Variable que define si se estan haciendo pruebas o no.
    // Se hace public para que sea lo mas parecido a una variable global.
    public static final boolean DEBUG = false;

    // Implementaci?n GEN?TICA.s

    // Poblaci?n: 50 individuos.
    private int maxPopulation = 50;                                  // Poblacion o numero de individuos.
    private int maxIterations = 500;                                 // N?mero maximo de iteraciones del proceso evolutivo.
    public static final int GAP=1;                                          // Se usa en initializePopulation.
    public static final int PLATFORM=2;
    public static final int HILL=3;
    public static final int CANNON=4;
    public static final int TUBE=5;


    private final int crossProbability = 30, mutationProbability = 10, desiredDifficulty = 100;
    private final int mutationNumLevels = (int) (0.1 * maxPopulation);
    private float [] fitnessValues;

    private static Random levelSeedRandom = new Random();

    private int height, width;

    // Fenotipo: Sera una coleccian de arrays (cromosomas) de arrays de flotantes (genes).
    private ArrayList<Individual> phenotype = new ArrayList<Individual>(maxPopulation);

    public GeneticLevelGenerator(int h, int w) {
        height = h;
        width = w;
    }

    // Inicializacion de la poblacion.
    void initializePopulation () {

        // Rellenamos el fenotipo.
        for (int i=0; i<maxPopulation; i++)
            phenotype.add(new Individual());

        // Para cada elemento del fenotipo (nivel).
        for (Individual individual : phenotype) {

            int accumulativeWidth = 0, individualIndex = 0;

            boolean legalElement = true;

            // Para cada elemento del nivel.
            for (LevelElement element : individual.getIndividual()) {

                int levelElement = levelSeedRandom.nextInt(4) + 1;

                if (legalElement) {
                    element.setElementType(levelElement);
                    //element.setX(levelSeedRandom.nextInt(91) + 5);
                    element.setX(accumulativeWidth);
                    element.setY(levelSeedRandom.nextInt(3) + 3);
                }
                else {
                    element.setElementType(PLATFORM);
                    element.setX(accumulativeWidth);
                    element.setY(1);
                }

                if (element.getElementType() == GAP)
                    element.setParam1(levelSeedRandom.nextInt(2) + 3);
                else
                    element.setParam1(levelSeedRandom.nextInt(2) + 7);

                accumulativeWidth += element.getParam1();

                if (levelElement == HILL)
                    individual.addGeneticElement(individualIndex);
                    element.setParam2(levelSeedRandom.nextInt(5));                                                      // Tipo del enemigo, entre 0 y 4 segun Enemy.java.
                    element.setParam3(levelSeedRandom.nextInt(3)+1);                                                    // Numero de enemigos, entre 1 y 3.

                individualIndex++;

                // Un nivel tendra 30 elementos o menos si la longitud del nivel supera el maximo impuesto (width).
                if (accumulativeWidth >= width-64)
                    break;

                legalElement = (element.getElementType() != GAP);
            }
        }
    }

    // Evaluacion: Tomara la poblacion en el momento en que se llama y devolvera el valor fitness de cada individuo.
    private void evaluate(float[] fitnessValues) {

        // Valoramos la dificultad de cada enemigo y la cantidad de estos en el nivel.
        for (Individual level: phenotype) {

            float accumulate = 0;

            for (Integer geneticElem: level.getGeneticElements()) {

                LevelElement elem = level.getElement(geneticElem);
                int enemyType = elem.getParam2();

                if (enemyType == Enemy.ENEMY_SPIKY)
                    enemyType = 4;
                else if (enemyType == Enemy.ENEMY_FLOWER)
                    enemyType = 3;

                accumulate += (enemyType + 1)*elem.getParam3();                                                         // Producto del tipo de enemigo por el numero de enemigos de este tipo en cada elemento gen�tico (por ahora Hills).
            }

            fitnessValues[phenotype.indexOf(level)] = Math.abs(accumulate - desiredDifficulty);                         // Cuanto se acerca la dificultad del nivel a lo que buscamos?
        }
    }

    // Cruce: Se reciben los dos padres y a partir de ellos se obtiene un hijo.
    private Individual crossOperator (Individual parent1, Individual parent2) {

        // Se crea un hijo igual que el primer padre.
        Individual child = new Individual(parent1);

        long seed = System.nanoTime();

        ArrayList<Integer> geneticElements1 = parent1.getGeneticElements();
        ArrayList<Integer> geneticElements2 = parent2.getGeneticElements();

        // Aleatorizamos los valores.
        Collections.shuffle(geneticElements1, new Random(seed));
        Collections.shuffle(geneticElements2, new Random(seed));

        if (DEBUG) {
            System.out.println("\n >> Valores fitness de los padres <<");
            System.out.println("    - Nivel (padre 1): " + phenotype.indexOf(parent1) + ", valor FITNESS = " + fitnessValues[phenotype.indexOf(parent1)]);
            System.out.println("    - Nivel (padre 2): " + phenotype.indexOf(parent2) + ", valor FITNESS = " + fitnessValues[phenotype.indexOf(parent2)]);
            System.out.println(" ** Fin valores fitness de los padres **");
        }

        int minGeneticElements = Math.min(geneticElements1.size(), geneticElements2.size());
        int elementsToCross = levelSeedRandom.nextInt(minGeneticElements);

        for (int i=0; i<elementsToCross; i++) {

            int randomLevelElement = levelSeedRandom.nextInt(minGeneticElements);

            // Como el hijo es igual al padre 1, entrar por aqui significa cambiar los datos del elemento seleccionado por los del padre 2.
            if ((randomLevelElement != -1)) {
                LevelElement parent2Values = parent2.getElement(geneticElements2.get(randomLevelElement));
                child.setElementParam(geneticElements1.get(randomLevelElement), 2, parent2Values.getParam2());          // Cambiamos el tipo de enemigo (param2).
                child.setElementParam(geneticElements1.get(randomLevelElement), 3, parent2Values.getParam3());          // Cambiamos el numero de enemigos (param3).
            }
        }

        return child;
    }

    // Mutacion: Tomara aleatoriamente miembros de la poblacion y realizara pequenas modificaciones en ellos.
    private void mutation () {

        for (int i=0; i<mutationNumLevels; i++) {

            Individual level = phenotype.get(levelSeedRandom.nextInt(maxPopulation));

            for (Integer geneticElem: level.getGeneticElements()) {
                int random = levelSeedRandom.nextInt(100);

                if (random < mutationProbability) {
                    level.setElementParam(geneticElem, 2, levelSeedRandom.nextInt(5));                                  // Hacemos las mutaciones como en la inicializacion de la poblacion (5 y 4).
                    level.setElementParam(geneticElem, 3, levelSeedRandom.nextInt(3)+1);
                }
            }

        }
    }

    // Reemplazamiento: Del hijo obtenido se reemplaza el peor nivel de la poblacion -> Nivel con mayor fitness.
    private void populationReplacement (Individual child, float [] fitnessValues) {

        // Obtenemos el indice del peor nivel de la poblacion.
        int worstLevel = 0;
        float max = -1;

        for (int i=0; i<maxPopulation; i++) {
            if (fitnessValues[i] > max) {
                worstLevel = i;
                max = fitnessValues[i];
            }
        }

        // Calculamos el valor fitness del hijo.
        float accumulate = 0, childFitness;

        for (Integer geneticElem: child.getGeneticElements()) {

            LevelElement elem = child.getElement(geneticElem);
            accumulate += (elem.getParam2() + 1)*elem.getParam3();
        }

        childFitness = Math.abs(accumulate - desiredDifficulty);

        if (DEBUG) {
            System.out.println("\n >> Valor fitness del peor nivel <<");
            System.out.println("    - Nivel: " + worstLevel + ", valor FITNESS = " + fitnessValues[worstLevel]);
            System.out.println(" ** Fin valor fitness del peor nivel **");

            System.out.println("\n >> Valor fitness del hijo <<");
            System.out.println("    - Valor FITNESS = " + childFitness);
            System.out.println(" ** Fin valor fitness del hijo **");
        }

        if (childFitness < fitnessValues[worstLevel]) {                                                                 // Reemplazamos el hijo por el peor nivel si lo mejora en fitness (su valor es menor).
            phenotype.set(worstLevel, child);
            fitnessValues[worstLevel] = childFitness;
        }

    }

    // Funcion de generaci?n de nivel genetico.
    Individual createLevelGen (long seed) {

        // Evaluacion del tiempo de ejecucion.
        long lStartTime = System.nanoTime();

        // Array de valores fitness. Al declararlo asi, fitnessvalues tendra una direccion de memoria,
        // haciendo que el paso de parametros sea por referencia.
        fitnessValues = new float [maxPopulation];

        // Numero de iteraciones del proceso evolutivo. Debe ser menor que maxIterations.
        int numIterations = 0,  bestIteration = -1;
        int tournamentIterations = 0;
        int [] selectedParents = new int[2];
        Individual child = null;
        Object [] bestSolution = new Object[2];

        bestSolution[0] = new Float(9999);
        bestSolution[1] = new Integer(0);

        // Inicializamos el generador con la semilla.
        levelSeedRandom.setSeed(seed);

        // 1. Inicializacion de la poblaci?n: Aleatoriamente.
        initializePopulation();

        // 2. Evaluacion inicial de la poblacion.
        evaluate(fitnessValues);

        if (DEBUG)
            printFitnessValues();

        // 3. Bucle principal, donde se realiza el proceso evolutivo.
        do {

            // 3.1. Seleccion de padres: Torneo binario. Se elige el mejor de dos padres, se hace dos veces (binario).
            do {
                int firstParent = levelSeedRandom.nextInt(50);
                int secondParent = levelSeedRandom.nextInt(50);

                if (firstParent != secondParent && (firstParent != selectedParents[0] && secondParent != selectedParents[0])) {
                    selectedParents[tournamentIterations] = fitnessValues[firstParent] < fitnessValues[secondParent] ? firstParent : secondParent;
                    tournamentIterations++;
                }

            } while (tournamentIterations < 2);

            // 3.2. Cruce de los dos padres El hijo debe ser un nivel válido. Probabilidad de cruce: 100%.
            int crossProb = -1;

            if (crossProb < crossProbability)
                child = crossOperator (phenotype.get(selectedParents[0]), phenotype.get(selectedParents[1]));

            // 3.3 Mutacion.
            mutation();

            // 3.4. Reemplazamiento. Decidir.
            populationReplacement(child, fitnessValues);

            // 3.5. Evaluacion de la poblaci?n.
            evaluate (fitnessValues);

            // 3.6. Actualizar la mejor solucion.
            for (int i=0; i<maxPopulation; i++) {
                if (fitnessValues[i] < (float) bestSolution[0]) {
                    bestSolution[1] = i;
                    bestSolution[0] = fitnessValues[i];
                    bestIteration = numIterations;

                    if (DEBUG) {
                        System.out.println("\n >> Mejor solución actualizada <<");
                        System.out.println("    - Nivel " + bestSolution[1] + " FITNESS = " + bestSolution[0]);
                        System.out.println(" ** Fin mejor solucion actualizada **");
                    }
                }
            }

            if (DEBUG) {
                System.out.println("\n --> Valores fitness en la iteración " + numIterations + " <--");
                printFitnessValues();

            }

            numIterations++;
            tournamentIterations = 0;
        } while (numIterations < maxIterations && ((float) bestSolution[0] != 0.0));

        long lEndTime = System.nanoTime();
        long executionTime =  (lEndTime-lStartTime)/1000000;

        System.out.println("Tiempo de ejecucion: " + executionTime);

        System.out.println("    - Nivel " + bestSolution[1] + " FITNESS = " + bestSolution[0] + " en la iteracion " + bestIteration);

        if (DEBUG) {
            System.out.println("\n >> Valor fitness de la mejor solucion <<");
            System.out.println("    - Nivel " + bestSolution[1] + " FITNESS = " + bestSolution[0] + " en la iteración " + bestIteration);
            System.out.println(" ** Fin valor fitness de la mejor solucion **");
        }

        return phenotype.get((int) bestSolution[1]);

    }

    private void printFitnessValues() {

        System.out.println("\n >> Valores fitness de la poblaci�n <<");

        for (int i=0; i<maxPopulation; i++) {
            System.out.println("    - Nivel: " + i + ", valor FITNESS = " + fitnessValues[i]);
        }

        System.out.println(" ** Fin valores fitness de la poblaci�n **");
    }
}
