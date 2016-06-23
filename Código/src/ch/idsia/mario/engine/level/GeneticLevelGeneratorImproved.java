package ch.idsia.mario.engine.level;

import java.lang.reflect.Array;
import java.util.*;

import ch.idsia.mario.engine.sprites.Enemy;
import org.json.*;

/**
 * Created by roka9 on 14/06/2016.
 */
public class GeneticLevelGeneratorImproved {

    // Variable que define si se estan haciendo pruebas o no. Se hace public para que sea lo mas parecido a una variable global.
    public static final boolean DEBUG = true;

    // Implementaci?n GEN?TICA.s

    // Poblaci?n: 50 individuos.
    private int maxPopulation = 1;                                  // Poblaci?n o n?mero de individuos.
    private int maxIterations = 500;                                 // N?mero m?ximo de iteraciones del proceso evolutivo.
    public static final int PLATFORM=1;                                          // Se usa en initializePopulation.
    public static final int CANNON=2;
    public static final int HILL=3;
    public static final int GAP=4;
    public static final int TUBE=5;
    private static final int CANNON_HILL=6;
    private static final int TUBE_HILL=7;
    private static final int COIN=8;
    private static final int BLOCK_COIN=9;
    private static final int BLOCK_POWERUP=10;
    private static final int ROCK_COIN=11;
    private static final int ROCK_EMPTY=12;
    private static final int KOOPA=13;
    private static final int GOOMPA=14;

    private final int crossProbability = 30, mutationProbability = 10, desiredDifficulty = 50, initialDifficulty = 1;
    private final int mutationNumLevels = (int) (0.1 * maxPopulation);
    private float [] fitnessValues;

    private static Random levelSeedRandom = new Random();

    private int height, width;

    // Esto estaba antes en la clase como private, pero quiero probar a hacer vectores est?ticos, mucho m?s eficiente (creo). Reservamos espacio suficiente.
    // Fenotipo: Sera una coleccian de arrays (cromosomas) de arrays de flotantes (genes).
    private ArrayList<Individual> phenotype = new ArrayList<Individual>(maxPopulation);

    public GeneticLevelGeneratorImproved(int h, int w) {
        height = h;
        width = w;
    }

    /*
    TEST:  MÉTODO DE INICIALIZACIÓN
        - Añadir algo más de variedad, elementos deseables en el nivel final a nivel estructural.
     */

    // Inicializacion de la poblacion: Aleatorio.
    void initializePopulation () {

        // Rellenamos el fenotipo.
        for (int i=0; i<maxPopulation; i++)
            phenotype.add(new Individual());

        // Parámetros de dificultad.
        switch (initialDifficulty) {
            case 1:
                initializePopulationEasy();
                break;

            case 2:
                initializePopulationMedium();
                break;

            case 3:
                initializePopulationHard();
                break;

            default:
                initializePopulationMedium();
                break;
        }

    }

    /*
    TEST: NUEVA FUNCIÓN DE EVALUACIÓN.
        - Considerar los enemigos.
        - Considerar obstáculos estructurales (huecos, colinas...)
     */

    // Evaluaci?n: Tomar? la poblaci?n en el momento en que se llama y devolver? el valor fitness de cada individuo.
    private void evaluate (float[] fitnessValues) {

        // Valoramos la dificultad de cada enemigo y la cantidad de estos en el nivel. Se tienen en cuenta
        // los elementos estructurales como la dificultad estructural media del nivel.
        for (Individual level: phenotype) {

            float accumulate = level.getStructuralDifficulty();

            for (Integer geneticElem: level.getGeneticElements()) {

                LevelElement elem = level.getElement(geneticElem);
                accumulate += (elem.getParam2() + 1)*elem.getParam3();                                                  // Producto del tipo de enemigo por el n?mero de enemigos de este tipo en cada elemento gen?tico (por ahora Hills).
            }

            fitnessValues[phenotype.indexOf(level)] = Math.abs(accumulate - desiredDifficulty);                         // ?Cuanto se acerca la dificultad del nivel a lo que buscamos?
        }
    }
    /*
    TEST: NUEVO MÉTODO DE CRUCE:
        - Juego entre exploración y explotación.
        - Otros métodos, BLX-alfa, CHC...
        - ¿Operador que vaya cambiando durante la ejecución?.
        - Probar a generar dos hijos en vez de uno.
        - Añadir elementos estructurales al cruce -> Problema: Asegurar que el nivel sea finalizable.
     */
    // Cruce: Se reciben los dos padres y a partir de ellos se obtiene un hijo.
    private Individual crossOperator (Individual parent1, Individual parent2) {

        Individual child = new Individual(parent1);

        if (DEBUG) {
            System.out.println("\n >> Valores fitness de los padres <<");
            System.out.println("    - Nivel (padre 1): " + phenotype.indexOf(parent1) + ", valor FITNESS = " + fitnessValues[phenotype.indexOf(parent1)]);
            System.out.println("    - Nivel (padre 2): " + phenotype.indexOf(parent2) + ", valor FITNESS = " + fitnessValues[phenotype.indexOf(parent2)]);
            System.out.println(" ** Fin valores fitness de los padres **");
        }

        // Se busca el primer elemento del segundo padre a partir del índice que puede colocarse en
        // el hijo (copia del primer padre). La condición es el índice de dificultad, pues el hijo
        // debe ser coherente con éste. El elemento a tener en cuenta en esta situación es GAP.

         // El índice por donde empezar a buscar se selecciona como la mitad exacta del hijo, más/menos 1/6 de su longitud
        // máxima (5 en este caso).
        int childIndex = getCrossIndex(child, 5, 5);
        int childElement = child.getElement(childIndex).getElementType();

        // Se selecciona un índice aleatorio en el padre 2, lo más centrado posible en la mitad del individuo.
        int p2Index = getCrossIndex(parent2, 5, 5);

        // Sólo tenemos problemas estructurales si el tipo del elemento en el hijo es Hueco.
        if (childElement == GAP) {

            boolean elementSelected = false;

            // Debemos asegurar que se elige un elemento del padre2, de lo contrario no haremos nada.
            while (!elementSelected) {

                do {
                    switch (initialDifficulty) {

                        // Dificultad FÁCIL.
                        case 1:
                            if (parent2.getElement(p2Index).getElementType() == PLATFORM)
                                elementSelected = true;

                            break;

                        // Dificultad MEDIA.
                        case 2:
                            if (parent2.getElement(p2Index).getElementType() == PLATFORM || parent2.getElement(p2Index).getElementType() == HILL)
                                elementSelected = true;

                            break;

                        // Dificultad DIFÍCIL.
                        case 3:
                            if (parent2.getElement(p2Index).getElementType() == CANNON)
                                elementSelected = true;

                            break;
                    }

                    p2Index++;
                } while (p2Index < parent2.getIndividual().size() && !elementSelected);
            }
        }

        // Se eliminan los elementos genéticos que ya no son necesarios en el hijo.
        ArrayList<Integer> childGeneticElements = child.getGeneticElements();
        int geneticElementsSize = childGeneticElements.size();

        for (int i=0; i<geneticElementsSize; i++) {
            if (childGeneticElements.get(i) >= childIndex) {
                child.deleteGeneticElement(i);
            }
        }

        // Llegados a esta altura, tenemos dos índices a partir de los cuales se puede.
        // Copiar elementos a partir del índice seleccionado en el padre 2 al hijo a partir del índice crossIndex.
        int maxCrossElements = Math.min((parent2.getIndividual()).size() - p2Index, (child.getIndividual()).size() - childIndex);
        int childCurrentIndex = childIndex;

        for (int i=p2Index; i<maxCrossElements; i++) {

            LevelElement currentChildElement = child.getElement(childCurrentIndex), currentP2Element = parent2.getElement(i);

            currentChildElement.setElementType(currentP2Element.getElementType());
            currentChildElement.setParam1(currentP2Element.getParam1());
            currentChildElement.setParam2(currentP2Element.getParam2());
            currentChildElement.setParam3(currentP2Element.getParam3());

            // Si el elemento insertado es una colina, se inserta su índice en geneticElements.
            if (currentChildElement.getElementType() == HILL){
                child.addGeneticElement(childCurrentIndex);
            }

            childCurrentIndex++;
        }

        return child;
    }


    private int getCrossIndex(Individual individual, int minOffset, int maxOffset) {
        int halfIndividual = (individual.getIndividual()).size();
        int min = halfIndividual - minOffset, max = halfIndividual + maxOffset;
        return min + levelSeedRandom.nextInt(max-min);

    }

    // Mutacion: Tomara aleatoriamente miembros de la poblacion y realizara pequenas modificaciones en ellos.
    // SE VA A QUEDAR COMO ESTÁ.
    private void mutation () {

        for (int i=0; i<mutationNumLevels; i++) {

            Individual level = phenotype.get(levelSeedRandom.nextInt(maxPopulation));

            for (Integer geneticElem: level.getGeneticElements()) {
                int random = levelSeedRandom.nextInt(100);

                if (random < mutationProbability) {
                    level.setElementParam(geneticElem, 2, levelSeedRandom.nextInt(5));                                  // Hacemos las mutaciones como en la inicializaci?n de la poblaci?n (5 y 4).
                    level.setElementParam(geneticElem, 3, levelSeedRandom.nextInt(3)+1);
                }
            }

        }
    }

    /*
    TODO: NUEVO MÉTODO DE REEMPLAZAMIENTO
        - Estudiar bien los resultados y añadir o eliminar exploración/explotación.
        -> Con el método de cruce utilizado se añade bastante diversidad, además de la mutación, establecer un reemplazamiento basado
        en la explotación.
     */

    // Reemplazamiento: Del hijo obtenido se reemplaza el peor nivel de la poblacion -> Nivel con mayor fitness (se distancia mas de lo que buscamos).
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

        if (childFitness < fitnessValues[worstLevel]) {                                                                  // Reemplazamos el hijo por el peor nivel si lo mejora en fitness (su valor es menor).
            phenotype.set(worstLevel, child);
            fitnessValues[worstLevel] = childFitness;
        }

    }

    // Funci?n de generaci?n de nivel GEN?TICO.
    Individual createLevelGenImproved (long seed) {
        /*
        // Array de valores fitness. Al declararlo as?, fitness values tendr? una direcci?n de memoria, haciendo que el paso de par?metros sea por referencia.
        fitnessValues = new float [maxPopulation];

        // N?mero de iteraciones del proceso evolutivo. Debe ser menor que maxIterations.
        int numIterations = 0;
        int tournamentIterations = 0;
        int [] selectedParents = new int[2];
        Individual child = null;
        Object [] bestSolution = new Object[2];

        bestSolution[0] = new Float(9999);
        bestSolution[1] = new Integer(0);

        // Inicializamos el generador con la semilla.
        levelSeedRandom.setSeed(seed);

        // 1. Inicializaci?n de la poblaci?n: Aleatoriamente.
        initializePopulation();

        // 2. Evaluaci?n inicial de la poblaci?n.
        evaluate(fitnessValues);

        if (DEBUG)
            printFitnessValues();

        // 3. Bucle principal, donde se realiza el proceso evolutivo.
        do {

            // 3.1. Selecci?n de padres: Torneo binario. Se elige el mejor de dos padres, se hace dos veces (binario).
            do {
                int firstParent = levelSeedRandom.nextInt(50);
                int secondParent = levelSeedRandom.nextInt(50);

                if (firstParent != secondParent && (firstParent != selectedParents[0] && secondParent != selectedParents[0])) {
                    selectedParents[tournamentIterations] = fitnessValues[firstParent] < fitnessValues[secondParent] ? firstParent : secondParent;
                    tournamentIterations++;
                }

            } while (tournamentIterations < 2);

            // 3.2. Cruce de los dos padres El hijo debe ser un nivel válido. Probabilidad de cruce: 100%.
            //int crossProb = levelSeedRandom.nextInt(100);
            int crossProb = -1;

            if (crossProb < crossProbability)
                child = crossOperator (phenotype.get(selectedParents[0]), phenotype.get(selectedParents[1]));

            // 3.3 Mutaci?n.
            mutation();

            // 3.4. Reemplazamiento. Decidir.
            populationReplacement(child, fitnessValues);

            // 3.5. Evaluaci?n de la poblaci?n.
            evaluate (fitnessValues);

            // 3.6. Actualizar la mejor solucion.
            for (int i=0; i<maxPopulation; i++) {
                if (fitnessValues[i] < (float) bestSolution[0]) {
                    bestSolution[1] = i;
                    bestSolution[0] = fitnessValues[i];

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
        } while (numIterations < maxIterations);


        if (DEBUG) {
            System.out.println("\n >> Valor fitness de la mejor solucion <<");
            System.out.println("    - Nivel " + bestSolution[1] + " FITNESS = " + bestSolution[0]);
            System.out.println(" ** Fin valor fitness de la mejor solucion **");
        }


        return phenotype.get((int) bestSolution[1]);
        */

        initializePopulation();
        return phenotype.get(0);
    }


    /* Nivel de dificultad fácil:
        - Longitudes de elementos aumentadas (PARAM1).
        - Los elementos estructurales más complicados para el jugador tienen menos probabilidad (GAP, CANNON y HILL).
        - Después de un salto SIEMPRE se genera una plataforma.
        - No se incluye SPINY como enemigo, es demasiado difícil.
        - Las tortugas tienen una mayor probabilidad de aparecer (35%).
        - Menor variación vertical del nivel.
    */
    private void initializePopulationEasy() {

        // Para cada elemento del fenotipo (nivel).
        for (Individual individual : phenotype) {

            int accumulativeWidth = 0, individualIndex = 0;
            boolean gapGenerated = false;

            for (LevelElement element : individual.getIndividual()) {

                //float levelElement = levelSeedRandom.nextFloat();
                float levelElement = (float) 0.5;

                element.setParam1(levelSeedRandom.nextInt(2) + 10);                                                         // Ancho del elemento.

                // Después de un hueco SIEMPRE va una plataforma.
                if (levelElement < 0.15 && !gapGenerated) {                                                                 // Hueco: Menor probabilidad de aparecer.
                    element.setElementType(GAP);
                    element.setParam1(levelSeedRandom.nextInt(2) + 1);                                                      // Tienen una menor anchura.
                } else if (levelElement >= 0.15 && levelElement < 0.35 && !gapGenerated) {                                    // Cañón
                    element.setElementType(CANNON);
                } else if (levelElement >= 0.35 && levelElement < 0.65 && !gapGenerated) {                                    // Colina
                    element.setElementType(HILL);
                    individual.addGeneticElement(individualIndex);                                                          // Se añade un registro de qué

                    float turtleEnemyProb = levelSeedRandom.nextFloat();
                    int enemyType = levelSeedRandom.nextInt(4);

                    if (turtleEnemyProb <= 0.35)                                                                            // El KOOPA verde (más fácil que el rojo) tiene más posibilidades de aparecer.
                        enemyType = Enemy.ENEMY_GREEN_KOOPA;

                    element.setParam2(enemyType);                                                                           // A?adimos el tipo del enemigo, entre 0 y 3 segun Enemy.java. NO SE INCLUYE SPINY
                    element.setParam3(levelSeedRandom.nextInt(2) + 1);                                                      // A?adimos el numero de enemigos, entre 1 y 2.
                } else {                                                                                                      // Plataforma
                    element.setElementType(PLATFORM);
                }

                element.setX(accumulativeWidth);                                                                            // Posición horizontal inicial del bloque estructural
                element.setY(levelSeedRandom.nextInt(3) + 2);                                                               // Posición vertical inicial del bloque estructural
                accumulativeWidth += element.getParam1();

                individualIndex++;

                gapGenerated = (element.getElementType() == GAP);

                // Un nivel tendra 30 elementos o menos si la longitud del nivel supera el maximo impuesto (width).
                if (accumulativeWidth >= width - 64)
                    break;
            }
        }
    }

    /* Nivel de dificultad medio:
    - Longitudes de elementos aumentadas (PARAM1).
    - Los elementos estructurales más complicados para el jugador tienen menos probabilidad (GAP, CANNON y HILL).
    - Después de un salto SIEMPRE se genera una plataforma.
    - No se incluye SPINY como enemigo, es demasiado difícil.
    - Las tortugas tienen una mayor probabilidad de aparecer (35%).
    - Menor variación vertical del nivel.
    */
    private void initializePopulationMedium() {

        // Para cada elemento del fenotipo (nivel).
        for (Individual individual : phenotype) {

            int accumulativeWidth = 0, individualIndex = 0;
            boolean gapGenerated = false;

            for (LevelElement element : individual.getIndividual()) {

                //float levelElement = levelSeedRandom.nextFloat();
                float levelElement = (float) 0.5;

                element.setParam1(levelSeedRandom.nextInt(2) + 8);                                                          // Ancho del elemento, un poco más corto.

                // Después de un hueco SIEMPRE va una plataforma.
                if (levelElement < 0.2 && !gapGenerated) {                                                                  // Hueco: Menor probabilidad de aparecer. No se puede generar después de un hueco.
                    element.setElementType(GAP);
                    element.setParam1(levelSeedRandom.nextInt(2) + 1);                                                      // Tienen una menor anchura.
                } else if (levelElement >= 0.2 && levelElement < 0.45 && !gapGenerated) {                                     // Cañón. No se puede generar después de un hueco.
                    element.setElementType(CANNON);
                } else if (levelElement >= 0.45 && levelElement < 0.70) {                                                     // Colina, ahora puede generarse después de un hueco.
                    element.setElementType(HILL);
                    individual.addGeneticElement(individualIndex);                                                          // Se añade un registro de qué

                    float enemyProb = levelSeedRandom.nextFloat();
                    int enemyType = levelSeedRandom.nextInt(4);

                    if (enemyProb <= 0.1)
                        enemyType = Enemy.ENEMY_SPIKY;                                                                      // Se incluye el spiky con muy baja probabilidad
                    else if (enemyProb <= 0.35)                                                                             // El KOOPA verde (más fácil que el rojo) tiene más posibilidades de aparecer.
                        enemyType = Enemy.ENEMY_GREEN_KOOPA;

                    element.setParam2(enemyType);                                                                           // A?adimos el tipo del enemigo, entre 0 y 3 segun Enemy.java. NO SE INCLUYE SPINY
                    element.setParam3(levelSeedRandom.nextInt(2) + 2);                                                      // A?adimos el numero de enemigos, entre 1 y 3. Habrá más enemigos.
                } else {                                                                                                      // Plataforma
                    element.setElementType(PLATFORM);
                }

                element.setX(accumulativeWidth);                                                                            // Posición horizontal inicial del bloque estructural
                element.setY(levelSeedRandom.nextInt(3) + 3);                                                               // Posición vertical inicial del bloque estructural. Varía un poco más.
                accumulativeWidth += element.getParam1();

                individualIndex++;

                gapGenerated = (element.getElementType() == GAP);

                // Un nivel tendra 30 elementos o menos si la longitud del nivel supera el maximo impuesto (width).
                if (accumulativeWidth >= width - 64)
                    break;
            }
        }
    }


    /* Nivel de dificultad difícil:
    - Longitudes de elementos aumentadas (PARAM1).
    - Los elementos estructurales más complicados para el jugador tienen menos probabilidad (GAP, CANNON y HILL).
    - Después de un salto SIEMPRE se genera una plataforma.
    - No se incluye SPINY como enemigo, es demasiado difícil.
    - Las tortugas tienen una mayor probabilidad de aparecer (35%).
    - Menor variación vertical del nivel.
    */
    private void initializePopulationHard() {

        // Para cada elemento del fenotipo (nivel).
        for (Individual individual : phenotype) {

            int accumulativeWidth = 0, individualIndex = 0;
            boolean gapGenerated = false;

            for (LevelElement element : individual.getIndividual()) {

                //float levelElement = levelSeedRandom.nextFloat();
                float levelElement = (float) 0.5;

                element.setParam1(levelSeedRandom.nextInt(2) + 5);                                                      // Ancho del elemento, un poco más corto.

                // Después de un hueco SIEMPRE va un cannon.
                if (levelElement < 0.35 && !gapGenerated) {                                                             // Hueco: Menor probabilidad de aparecer.
                    element.setElementType(GAP);
                    element.setParam1(levelSeedRandom.nextInt(2) + 1);                                                  // Tienen una menor anchura.
                } else if (gapGenerated || levelElement >= 0.35 && levelElement < 0.5) {                                // Cañón
                    element.setElementType(CANNON);
                } else if (levelElement >= 0.5 && levelElement < 0.8 && !gapGenerated) {                                // Colina, ahora puede generarse después de un hueco.
                    element.setElementType(HILL);
                    individual.addGeneticElement(individualIndex);

                    float enemyProb = levelSeedRandom.nextFloat();
                    int enemyType = levelSeedRandom.nextInt(4);

                    if (enemyProb <= 0.30)
                        enemyType = Enemy.ENEMY_SPIKY;                                                                  // Se incluye el spiky con probabilidad más alta.

                    element.setParam2(enemyType);                                                                       // A?adimos el tipo del enemigo, entre 0 y 3 segun Enemy.java. NO SE INCLUYE SPINY
                    element.setParam3(levelSeedRandom.nextInt(2) + 4);                                                  // A?adimos el numero de enemigos, entre 1 y 3. Habrá más enemigos.
                } else {                                                                                                // Plataforma
                    element.setElementType(PLATFORM);
                }

                element.setX(accumulativeWidth);                                                                        // Posición horizontal inicial del bloque estructural
                element.setY(levelSeedRandom.nextInt(3) + 4);                                                           // Posición vertical inicial del bloque estructural. Varía un poco más.
                accumulativeWidth += element.getParam1();

                individualIndex++;

                gapGenerated = (element.getElementType() == GAP);

                // Un nivel tendra 30 elementos o menos si la longitud del nivel supera el maximo impuesto (width).
                if (accumulativeWidth >= width - 64)
                    break;
            }
        }
    }

    private void printFitnessValues() {

        System.out.println("\n >> Valores fitness de la poblaci?n <<");

        for (int i=0; i<maxPopulation; i++) {
            System.out.println("    - Nivel: " + i + ", valor FITNESS = " + fitnessValues[i]);
        }

        System.out.println(" ** Fin valores fitness de la poblaci?n **");
    }
}
