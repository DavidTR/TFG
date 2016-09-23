package ch.idsia.mario.engine.third_solution;

import java.lang.reflect.Array;
import java.util.*;

import ch.idsia.mario.engine.level.Individual;
import ch.idsia.mario.engine.level.LevelElement;
import ch.idsia.mario.engine.sprites.Enemy;

/**
 * Created by roka9 on 14/06/2016.
 */
public class GeneticLevelGeneratorThirdSolution {

    // Variable que define si se estan haciendo pruebas o no.
    // Se hace public para que sea lo mas parecido a una variable global.
    public static final boolean DEBUG = false;

    // Poblacion: 50 individuos.
    private int maxPopulation = 50;                                                                                     // Poblacion o numero de individuos.
    private int maxIterations = 500;                                                                                    // Numero maximo de iteraciones del proceso evolutivo.
    public static final int PLATFORM=1;                                                                                 // Se usa en initializePopulation.
    public static final int CANNON=2;
    public static final int HILL=3;
    public static final int GAP=4;
    public static final int TUBE=5;

    private final int crossProbability = 30, mutationProbability = 10, desiredDifficulty = 50;
    public static final int initialDifficulty = 1;
    private final int mutationNumLevels = (int) (0.1 * maxPopulation);
    private float [] fitnessValues;

    private static Random levelSeedRandom = new Random();

    private int height, width;

    // Fenotipo: Sera una coleccian de arrays (cromosomas) de arrays de flotantes (genes).
    private ArrayList<IndividualThirdSolution> phenotype = new ArrayList<IndividualThirdSolution>(maxPopulation);

    public GeneticLevelGeneratorThirdSolution(int h, int w) {
        height = h;
        width = w;
    }

    // Inicializacion de la poblacion.
    void initializePopulation () {

        // Rellenamos el fenotipo.
        for (int i=0; i<maxPopulation; i++)
            phenotype.add(new IndividualThirdSolution());

        // Par�metros de dificultad.
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

    // Evaluacion: Tomara la poblacion en el momento en que se llama y devolver? el valor fitness de cada individuo.
    private void evaluate (float[] fitnessValues) {

        // Valoramos la dificultad de cada enemigo y la cantidad de estos en el nivel. Se tienen en cuenta
        // los elementos estructurales como la dificultad estructural media del nivel.
        for (IndividualThirdSolution level: phenotype) {

            float accumulate = 0;

            for (Integer geneticElem: level.getGeneticElements()) {

                LevelElementThirdSolution elem = level.getElement(geneticElem);

                // Este parche ha de hacerse porque SPIKY es el enemigo m�s dif�cil del juego, pero
                // si cambiamos de orden los �ndices que los representan (FLOWER = 3 y SPIKY = 4),
                // el juego no renderiza bien el modelo de los enemigos -> Utiliza constantes :(.
                int enemyType = elem.getParam2();

                if (enemyType == Enemy.ENEMY_SPIKY)
                    enemyType = 4;
                else if (enemyType == Enemy.ENEMY_FLOWER)
                    enemyType = 3;

                accumulate += (enemyType + 1)*elem.getParam3();                                                         // Producto del tipo de enemigo por el n?mero de enemigos de este tipo en cada elemento gen?tico (por ahora Hills).

            }

            // La dificultad estructural suma m�s conforme m�s grande sea, ya que es una media.
            // Esto significa que un valor medio de 3 aporta m�s dificultad que uno de 1.
            float structuralDifficulty = level.getStructuralDifficulty();

            if (structuralDifficulty >= 1 && structuralDifficulty <= 1.5)
                structuralDifficulty *= 10;
            else if (structuralDifficulty > 1.5 && structuralDifficulty <= 2.5)
                structuralDifficulty *= 20;
            else
                structuralDifficulty *= 40;

            //System.out.println("DIFICULTAD BRUTA = " + accumulate + structuralDifficulty);

            fitnessValues[phenotype.indexOf(level)] = Math.abs(accumulate + structuralDifficulty - desiredDifficulty);
        }
    }

    // Cruce: Se reciben los dos padres y a partir de ellos se obtiene un hijo.
    private IndividualThirdSolution crossOperator (IndividualThirdSolution parent1, IndividualThirdSolution parent2) {

        IndividualThirdSolution child = new IndividualThirdSolution(parent1);

        // Obtener la longtiud real de los elementos.
        int parent2Half = 0, childHalf = 0;

        while ( parent2Half < parent2.getIndividual().size() && parent2.getElement(parent2Half).getElementType() != -1)
            parent2Half++;

        while (childHalf < child.getIndividual().size() && child.getElement(childHalf).getElementType() != -1)
            childHalf++;

        parent2Half /= 2;
        childHalf /= 2;

        if (DEBUG) {
            System.out.println("\n >> Valores fitness de los padres <<");
            System.out.println("    - Nivel (padre 1): " + phenotype.indexOf(parent1) + ", valor FITNESS = " + fitnessValues[phenotype.indexOf(parent1)]);
            System.out.println("    - Nivel (padre 2): " + phenotype.indexOf(parent2) + ", valor FITNESS = " + fitnessValues[phenotype.indexOf(parent2)]);
            System.out.println(" ** Fin valores fitness de los padres **");
        }

        // Se busca el primer elemento del segundo padre a partir del �ndice que puede colocarse en
        // el hijo (copia del primer padre). La condici�n es el �ndice de dificultad, pues el hijo
        // debe ser coherente con �ste. El elemento a tener en cuenta en esta situaci�n es GAP.

         // El �ndice por donde empezar a buscar se selecciona como la mitad exacta del hijo, m�s/menos 1/6 de su longitud
        // m�xima (5 en este caso).
        int offset = 5;

        // Se selecciona un �ndice y se empieza a sustituir por el siguiente elemento, no justo por el seleccionado.
        int childIndex = getCrossIndex(childHalf, offset, 0);
        int childElement = child.getElement(childIndex).getElementType();

        // Se selecciona un �ndice aleatorio en el padre 2, lo m�s centrado posible en la mitad del individuo.
        int p2Index = getCrossIndex(parent2Half, offset, 0);

        // S�lo tenemos problemas estructurales si el tipo del elemento en el hijo es Hueco.
        if (childElement == GAP) {

            boolean elementSelected = false;

            // Debemos asegurar que se elige un elemento del padre2, de lo contrario no haremos nada.
            while (!elementSelected) {

                do {
                    switch (initialDifficulty) {

                        // Dificultad F�CIL.
                        case 1:
                            if (parent2.getElement(p2Index).getElementType() == PLATFORM)
                                elementSelected = true;

                            break;

                        // Dificultad MEDIA.
                        case 2:
                            if (parent2.getElement(p2Index).getElementType() == PLATFORM || parent2.getElement(p2Index).getElementType() == HILL)
                                elementSelected = true;

                            break;

                        // Dificultad DIF�CIL.
                        case 3:
                            if (parent2.getElement(p2Index).getElementType() == CANNON)
                                elementSelected = true;

                            break;
                    }

                    if (!elementSelected)
                        p2Index++;

                } while (p2Index < parent2.getIndividual().size() && !elementSelected);

                // Se selecciona un �ndice nuevo cada vez que  se de una vuelta a este bucle. Se va ampliando
                // si no se encuentra un buen punto. Se empieza en 5 y se va ampliando en cada iteraci�n.
                if (!elementSelected) {
                    offset++;
                    p2Index = getCrossIndex(parent2Half, offset, 0);
                }
            }
        }
        else {
            // Si el elemento actual no es un GAP existe la posbilidad de que se tengan 2 elementos repetidos.
            // Hay que comprobarlo.
            int currentChildElement = child.getElement(childIndex).getElementType();

            // Si esta condici�n es verdadera significa que el elemento de la posici�n actual en el hijo
            // es el mismo que el anterior, por lo que tendr�amos tres repetidos y tendr�amos que buscar otro que
            // no fuera del mismo tipo.
            // En este caso el nuevo �ndice del segundo padre se elige como en la segunda soluci�n (offset, offset).
            if (currentChildElement == child.getElement(childIndex-1).getElementType()) {
                boolean elementSelected = false;

                do {
                    if (parent2.getElement(p2Index).getElementType() != currentChildElement)
                        elementSelected = true;
                    else {
                        p2Index = getCrossIndex(parent2Half, offset, offset);
                        offset++;
                    }
                } while (!elementSelected);
            }
        }

        // Se eliminan los elementos gen�ticos que ya no son necesarios en el hijo.
        ArrayList<Integer> childGeneticElements = child.getGeneticElements();
        int childGeneticElementsSize = childGeneticElements.size();

        // Se empieza a sustituir por el siguiente elemento seleccionado, pues se sobreescribir�a.
        childIndex++;

        int j=0;

        while (j<childGeneticElementsSize) {
            if (childGeneticElements.get(j) >= childIndex) {
                child.deleteGeneticElement(j);
                childGeneticElementsSize--;
            }
            else
                j++;
        }

        // Llegados a esta altura, tenemos dos �ndices a partir de los cuales se pueden combinar los dos individuos.
        // Copiar elementos a partir del �ndice seleccionado en el padre 2 al hijo a partir del �ndice crossIndex.
        // Se copian tantos elementos como tengamos en el m�nimo entre ambos elementos restantes desde los �ndices seleccionados
        // hasta cada uno de los m�ximos de elementos.
        int maxCrossElements = Math.min((parent2.getIndividual()).size() - p2Index, (child.getIndividual()).size() - childIndex);
        int childCurrentIndex = childIndex;

        // Se hace una copia de elementos desde el padre 2 al hijo.
        // Se copian tantos elementos como sea posible -> maxCrossElements.
        for (int i=p2Index; i<p2Index+maxCrossElements; i++) {

            LevelElementThirdSolution currentChildElement = child.getElement(childCurrentIndex), currentP2Element = parent2.getElement(i);
            LevelElementThirdSolution previousChildElement = child.getElement(childCurrentIndex-1);

            currentChildElement.setElementType(currentP2Element.getElementType());
            currentChildElement.setParam1(currentP2Element.getParam1());
            currentChildElement.setParam2(currentP2Element.getParam2());
            currentChildElement.setParam3(currentP2Element.getParam3());

            // Si el elemento copiado es una colina, se inserta su �ndice en geneticElements.
            if (currentChildElement.getElementType() == HILL){
                child.addGeneticElement(childCurrentIndex);
            }

            //currentChildElement.setX(previousChildElement.getX()+currentChildElement.getParam1());
            currentChildElement.setX(previousChildElement.getX()+previousChildElement.getParam1());

            childCurrentIndex++;
        }

        return child;
    }

    // Genera un �ndice de cruce para un individuo. Se obtiene centrado en la mitad del mismo con una ligera variaci�n aleatoria.
    private int getCrossIndex(int halfIndividual, int minOffset, int maxOffset) {
        int min = halfIndividual - minOffset, max = halfIndividual + maxOffset;
        return min + levelSeedRandom.nextInt(max-min);
    }

    // Mutacion: Tomara aleatoriamente miembros de la poblacion y realizara pequenas modificaciones en ellos.
    private void mutation () {

        for (int i=0; i<mutationNumLevels; i++) {

            IndividualThirdSolution level = phenotype.get(levelSeedRandom.nextInt(maxPopulation));

            for (Integer geneticElem: level.getGeneticElements()) {
                int random = levelSeedRandom.nextInt(100);

                if (random < mutationProbability) {

                    // Intervalo en los que se van a mover los aleatorios generados en la mutaci�n (0 - valor).
                    int typesAllowed = 3, maxEnemies = 3;
                    switch(initialDifficulty) {
                        case 1:
                            maxEnemies = 1;
                            break;
                        case 2:
                            typesAllowed = 4;
                            maxEnemies = 2;
                            break;
                        case 3:
                            typesAllowed = 5;
                            maxEnemies = 3;
                            break;
                    }
                    level.setElementParam(geneticElem, 2, levelSeedRandom.nextInt(typesAllowed));
                    level.setElementParam(geneticElem, 3, levelSeedRandom.nextInt(2) + maxEnemies);
                }
            }
        }
    }

    // Reemplazamiento: Del hijo obtenido se reemplaza el peor nivel de la poblacion ->
    // Nivel con mayor fitness (se distancia mas de lo que buscamos).
    private void populationReplacement (IndividualThirdSolution child, float [] fitnessValues) {

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

            LevelElementThirdSolution elem = child.getElement(geneticElem);
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

    // Funci?n de generaci?n de nivel GEN?TICO.
    IndividualThirdSolution createLevelGenThirdSolution (long seed) {

        // Evaluacion del tiempo de ejecucion.
        long lStartTime = System.nanoTime();

        // Array de valores fitness. Al declararlo as?, fitness values tendr? una direcci?n de memoria, haciendo que el paso de par?metros sea por referencia.
        fitnessValues = new float [maxPopulation];

        // N?mero de iteraciones del proceso evolutivo.
        int numIterations = 0, bestIteration = -1;
        int tournamentIterations = 0;
        int [] selectedParents = new int[2];
        IndividualThirdSolution child = null;
        Object [] bestSolution = new Object[2];

        bestSolution[0] = new Float(9999);
        bestSolution[1] = new Float(0);

        // Inicializamos el generador con la semilla.
        levelSeedRandom.setSeed(seed);

        // 1. Inicializaci?n de la poblaci?n.
        initializePopulation();

        // 2. Evaluaci?n inicial de la poblaci?n.
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

            // 3.2. Cruce de los dos padres El hijo debe ser un nivel v�lido. Probabilidad de cruce: 100%.
            int crossProb = -1;

            if (crossProb < crossProbability)
                child = crossOperator (phenotype.get(selectedParents[0]), phenotype.get(selectedParents[1]));

            // 3.3 Mutacion.
            mutation();

            // 3.4. Reemplazamiento. Decidir.
            populationReplacement(child, fitnessValues);

            // 3.5. Evaluacion de la poblacion.
            evaluate (fitnessValues);

            // 3.6. Actualizar la mejor solucion.
            for (int i=0; i<maxPopulation; i++) {
                if (fitnessValues[i] < (float) bestSolution[0]) {
                    bestSolution[1] = i;
                    bestSolution[0] = fitnessValues[i];
                    bestIteration = numIterations;

                    if (DEBUG) {
                        System.out.println("\n >> Mejor soluci�n actualizada <<");
                        System.out.println("    - Nivel " + bestSolution[1] + " FITNESS = " + bestSolution[0]);
                        System.out.println(" ** Fin mejor solucion actualizada **");
                    }
                }
            }

            if (DEBUG) {
                System.out.println("\n --> Valores fitness en la iteraci�n " + numIterations + " <--");
                printFitnessValues();

            }

            numIterations++;
            tournamentIterations = 0;
        } while ((numIterations < maxIterations) && ((float) bestSolution[0] != 0.0));

        long lEndTime = System.nanoTime();
        long executionTime =  (lEndTime-lStartTime)/1000000;

        System.out.println("Tiempo de ejecucion: " + executionTime);

        System.out.println("    - Nivel " + bestSolution[1] + " FITNESS = " + bestSolution[0] + " en la iteracion " + bestIteration);



        if (DEBUG) {
            System.out.println("\n >> Valor fitness de la mejor solucion <<");
            System.out.println("    - Nivel " + bestSolution[1] + " FITNESS = " + bestSolution[0] + " en la iteraci�n " + bestIteration);
            System.out.println(" ** Fin valor fitness de la mejor solucion **");
        }

        return phenotype.get((int) bestSolution[1]);
    }

    /* Nivel de dificultad f�cil:
        - Longitudes de elementos aumentadas (PARAM1).
        - Los elementos estructurales m�s complicados para el jugador tienen menos probabilidad (GAP, CANNON y HILL).
        - Despu�s de un salto SIEMPRE se genera una plataforma.
        - No se incluye SPINY como enemigo, es demasiado dif�cil.
        - Las tortugas tienen una mayor probabilidad de aparecer (35%).
        - Menor variaci�n vertical del nivel.
    */
    private void initializePopulationEasy() {

        // Comprobar que no se genern m�s de dos elementos estructurales seguidos.
        // El array cuenta el n�mero de elementos seguidos de cada tipo generados.
        // Se comprueba que no se superen 2 elementos seguidos.
        int previousStructuralElement = 0, structuralCount = 0;

        // Para cada elemento del fenotipo (nivel).
        for (IndividualThirdSolution individual : phenotype) {

            int accumulativeWidth = 7, individualIndex = 0;
            boolean gapGenerated = false;

            for (LevelElementThirdSolution element : individual.getIndividual()) {

                boolean insertedElement = false;

                // Si no se inserta ning�n elemento, no se avanza al siguiente, de lo contrario
                // tendr�amos elementos sin inicializar en el individuo.
                while (!insertedElement) {

                    float levelElement = levelSeedRandom.nextFloat();

                    // Anchura b�sica
                    element.setParam1(levelSeedRandom.nextInt(2) + 10);                                                 // Ancho del elemento.

                    // Despu�s de un hueco SIEMPRE va una plataforma.
                    if (levelElement < 0.15 && !gapGenerated) {                                                         // Hueco: Menor probabilidad de aparecer -> 15%

                        // Por dise�o no puede haber 2 o m�s GAPs seguidos, no se hace la comprobaci�n.
                        element.setElementType(GAP);
                        element.setParam1(levelSeedRandom.nextInt(2) + 1);                                              // Tienen una menor anchura.

                        insertedElement = true;
                        structuralCount = 0;
                        previousStructuralElement = GAP;

                    } else if (levelElement >= 0.15 && levelElement < 0.35 && !gapGenerated) {                          // Ca��n -> 20%

                        if (!(previousStructuralElement == CANNON && structuralCount == 2)) {

                            element.setElementType(CANNON);
                            element.setParam1(levelSeedRandom.nextInt(5) + 5);

                            insertedElement = true;

                            if (previousStructuralElement == CANNON)
                                structuralCount++;
                            else {
                                structuralCount = 0;
                                previousStructuralElement = CANNON;
                            }
                        }

                    } else if (levelElement >= 0.35 && levelElement < 0.65 && !gapGenerated) {                          // Colina -> 30%

                        if (!(previousStructuralElement == HILL && structuralCount == 2)) {

                            element.setElementType(HILL);
                            individual.addGeneticElement(individualIndex);                                              // Se a�ade un registro de qu� elemento tiene enemigos.

                            float turtleEnemyProb = levelSeedRandom.nextFloat();
                            int enemyType = levelSeedRandom.nextInt(3);

                            if (turtleEnemyProb <= 0.35)                                                                // El KOOPA verde (m�s f�cil que el rojo) tiene m�s posibilidades de aparecer.
                                enemyType = Enemy.ENEMY_GREEN_KOOPA;

                            element.setParam2(enemyType);                                                               // A?adimos el tipo del enemigo, entre 0 y 3 segun Enemy.java. NO SE INCLUYE SPINY
                            element.setParam3(levelSeedRandom.nextInt(2) + 1);                                          // A?adimos el numero de enemigos, entre 2 y 3.

                            insertedElement = true;

                            if (previousStructuralElement == HILL)
                                structuralCount++;
                            else {
                                structuralCount = 0;
                                previousStructuralElement = HILL;
                            }
                        }
                    } else {                                                                                            // Plataforma -> 35%
                        if (!(previousStructuralElement == PLATFORM && structuralCount == 2)) {

                            element.setElementType(PLATFORM);

                            insertedElement = true;

                            if (previousStructuralElement == PLATFORM)
                                structuralCount++;
                            else {
                                structuralCount = 0;
                                previousStructuralElement = PLATFORM;
                            }
                        }
                    }

                    if (insertedElement) {
                        // Resto de par�metros (X, Y), com�n para todos.
                        element.setX(accumulativeWidth);                                                                // Posici�n horizontal inicial del bloque estructural
                        element.setY(levelSeedRandom.nextInt(2) + 10);                                                  // Posici�n vertical inicial del bloque estructural
                        accumulativeWidth += element.getParam1();

                        individualIndex++;

                        gapGenerated = (element.getElementType() == GAP);

                    }
                }

                // Un nivel tendra 30 elementos o menos si la longitud del nivel supera el maximo impuesto (width).
                if (accumulativeWidth >= width - 80)
                    break;
            }
        }
    }

    /* Nivel de dificultad medio:
    - Longitudes de elementos aumentadas (PARAM1).
    - Los elementos estructurales m�s complicados para el jugador tienen menos probabilidad (GAP, CANNON y HILL).
    - Despu�s de un salto SIEMPRE se genera una plataforma.
    - No se incluye SPINY como enemigo, es demasiado dif�cil.
    - Las tortugas tienen una mayor probabilidad de aparecer (35%).
    - Menor variaci�n vertical del nivel.
    */
    private void initializePopulationMedium() {

        // Comprobar que no se genern m�s de dos elementos estructurales seguidos.
        // El array cuenta el n�mero de elementos seguidos de cada tipo generados.
        // Se comprueba que no se superen 2 elementos seguidos.
        int previousStructuralElement = 0, structuralCount = 0;

        // Para cada elemento del fenotipo (nivel).
        for (IndividualThirdSolution individual : phenotype) {

            int accumulativeWidth = 7, individualIndex = 0;
            boolean gapGenerated = false;

            for (LevelElementThirdSolution element : individual.getIndividual()) {

                boolean insertedElement = false;

                // Si no se inserta ning�n elemento, no se avanza al siguiente, de lo contrario
                // tendr�amos elementos sin inicializar en el individuo.
                while (!insertedElement) {

                    float levelElement = levelSeedRandom.nextFloat();

                    // Anchura b�sica
                    element.setParam1(levelSeedRandom.nextInt(2) + 8);                                                  // Ancho del elemento, un poco m�s corto.

                    // Despu�s de un hueco puede ir una plataforma o colina.
                    if (levelElement < 0.2 && !gapGenerated) {                                                          // Hueco: Menor probabilidad de aparecer. No se puede generar despu�s de un hueco -> 20%
                        element.setElementType(GAP);
                        element.setParam1(levelSeedRandom.nextInt(2) + 4);                                              // Tienen una menor anchura.

                        insertedElement = true;
                        structuralCount = 0;
                        previousStructuralElement = GAP;

                    } else if (levelElement >= 0.2 && levelElement < 0.45 && !gapGenerated) {                           // Ca��n. No se puede generar despu�s de un hueco -> 25%

                        if (!(previousStructuralElement == CANNON && structuralCount == 2)) {

                            element.setElementType(CANNON);
                            element.setParam1(levelSeedRandom.nextInt(5) + 5);

                            insertedElement = true;

                            if (previousStructuralElement == CANNON)
                                structuralCount++;
                            else {
                                structuralCount = 0;
                                previousStructuralElement = CANNON;
                            }
                        }
                    } else if (levelElement >= 0.45 && levelElement < 0.70) {                                           // Colina, ahora puede generarse despu�s de un hueco -> 25%

                        if (!(previousStructuralElement == HILL && structuralCount == 2)) {

                            element.setElementType(HILL);
                            individual.addGeneticElement(individualIndex);                                              // Se a�ade un registro de qu� elementos tienen enemigos.

                            float enemyProb = levelSeedRandom.nextFloat();
                            int enemyType = levelSeedRandom.nextInt(4);

                            if (enemyProb <= 0.10)
                                enemyType = Enemy.ENEMY_SPIKY;                                                          // Se incluye el spiky con muy baja probabilidad
                            else if (enemyProb > 0.10 && enemyProb <= 0.35)                                             // El KOOPA verde (m�s f�cil que el rojo) tiene m�s posibilidades de aparecer.
                                enemyType = Enemy.ENEMY_GREEN_KOOPA;

                            element.setParam2(enemyType);                                                               // A?adimos el tipo del enemigo, entre 0 y 3 segun Enemy.java. NO SE INCLUYE SPINY
                            element.setParam3(levelSeedRandom.nextInt(2) + 2);                                          // A?adimos el numero de enemigos, entre 2 y 3. Habr� m�s enemigos.

                            insertedElement = true;


                            if (previousStructuralElement == HILL)
                                structuralCount++;
                            else {
                                structuralCount = 0;
                                previousStructuralElement = HILL;
                            }
                        }
                    } else {                                                                                            // Plataforma -> 30%
                        if (!(previousStructuralElement == PLATFORM && structuralCount == 2)) {

                            element.setElementType(PLATFORM);

                            insertedElement = true;

                            if (previousStructuralElement == PLATFORM)
                                structuralCount++;
                            else {
                                structuralCount = 0;
                                previousStructuralElement = PLATFORM;
                            }
                        }
                    }

                    if (insertedElement) {
                        // Resto de par�metros (X, Y), com�n para todos.
                        element.setX(accumulativeWidth);                                                                // Posici�n horizontal inicial del bloque estructural
                        element.setY(levelSeedRandom.nextInt(3) + 10);                                                  // Posici�n vertical inicial del bloque estructural. Var�a un poco m�s.
                        accumulativeWidth += element.getParam1();

                        individualIndex++;

                        gapGenerated = (element.getElementType() == GAP);
                    }
                }

                // Un nivel tendra 30 elementos o menos si la longitud del nivel supera el maximo impuesto (width).
                if (accumulativeWidth >= width - 60)
                    break;
            }
        }
    }


    /* Nivel de dificultad dif�cil:
    - Longitudes de elementos aumentadas (PARAM1).
    - Los elementos estructurales m�s complicados para el jugador tienen menos probabilidad (GAP, CANNON y HILL).
    - Despu�s de un salto SIEMPRE se genera una plataforma.
    - No se incluye SPINY como enemigo, es demasiado dif�cil.
    - Las tortugas tienen una mayor probabilidad de aparecer (35%).
    - Menor variaci�n vertical del nivel.
    */
    private void initializePopulationHard() {

        // Comprobar que no se genern m�s de dos elementos estructurales seguidos.
        // El array cuenta el n�mero de elementos seguidos de cada tipo generados.
        // Se comprueba que no se superen 2 elementos seguidos.
        int previousStructuralElement = 0, structuralCount = 0;

        // Para cada elemento del fenotipo (nivel).
        for (IndividualThirdSolution individual : phenotype) {

            int accumulativeWidth = 7, individualIndex = 0;
            boolean gapGenerated = false;

            for (LevelElementThirdSolution element : individual.getIndividual()) {

                boolean insertedElement = false;

                // Si no se inserta ning�n elemento, no se avanza al siguiente, de lo contrario
                // tendr�amos elementos sin inicializar en el individuo.
                while (!insertedElement) {

                    float levelElement = levelSeedRandom.nextFloat();

                    element.setParam1(levelSeedRandom.nextInt(2) + 7);                                                  // Ancho del elemento, un poco m�s corto.

                    // Despu�s de un hueco SIEMPRE va un cannon.
                    if (levelElement < 0.35 && !gapGenerated) {                                                         // Hueco: Mayor probabilidad de aparecer -> 35%
                        element.setElementType(GAP);
                        element.setParam1(levelSeedRandom.nextInt(2) + 4);                                              // Tienen una mayor anchura.

                        insertedElement = true;
                        structuralCount = 0;
                        previousStructuralElement = GAP;

                    } else if (gapGenerated || levelElement >= 0.35 && levelElement < 0.5) {                            // Ca��n -> 15%
                        if (!(previousStructuralElement == CANNON && structuralCount == 2)) {

                            element.setElementType(CANNON);
                            element.setParam1(levelSeedRandom.nextInt(5) + 5);

                            insertedElement = true;

                            if (previousStructuralElement == CANNON)
                                structuralCount++;
                            else {
                                structuralCount = 0;
                                previousStructuralElement = CANNON;
                            }
                        }
                    } else if (levelElement >= 0.5 && levelElement < 0.8 && !gapGenerated) {                            // Colina -> 30%
                        if (!(previousStructuralElement == HILL && structuralCount == 2)) {

                            element.setElementType(HILL);
                            individual.addGeneticElement(individualIndex);

                            float enemyProb = levelSeedRandom.nextFloat();
                            int enemyType = levelSeedRandom.nextInt(4);

                            if (enemyProb <= 0.35)
                                enemyType = Enemy.ENEMY_SPIKY;                                                          // Se incluye el spiky con probabilidad m�s alta.

                            element.setParam2(enemyType);                                                               // A?adimos el tipo del enemigo, entre 0 y 4 segun Enemy.java.

                            // S�lo cambia lo del spiky, de lo contrario ser�a muy jodido para el jugador.
                            element.setParam3(levelSeedRandom.nextInt(2) + 2);                                          // A?adimos el numero de enemigos, entre 4 y 5. Habr� m�s enemigos.

                            insertedElement = true;

                            if (previousStructuralElement == HILL)
                                structuralCount++;
                            else {
                                structuralCount = 0;
                                previousStructuralElement = HILL;
                            }
                        }
                    } else {                                                                                            // Plataforma -> 20%
                        if (!(previousStructuralElement == PLATFORM && structuralCount == 2)) {

                            element.setElementType(PLATFORM);

                            insertedElement = true;

                            if (previousStructuralElement == PLATFORM)
                                structuralCount++;
                            else {
                                structuralCount = 0;
                                previousStructuralElement = PLATFORM;
                            }
                        }
                    }

                    if (insertedElement) {
                        element.setX(accumulativeWidth);                                                                        // Posici�n horizontal inicial del bloque estructural
                        element.setY(levelSeedRandom.nextInt(4) + 10);                                                          // Posici�n vertical inicial del bloque estructural. Var�a un poco m�s.
                        accumulativeWidth += element.getParam1();

                        individualIndex++;

                        gapGenerated = (element.getElementType() == GAP);
                    }
                }

                // Un nivel tendra 30 elementos o menos si la longitud del nivel supera el maximo impuesto (width).
                if (accumulativeWidth >= width - 60)
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

