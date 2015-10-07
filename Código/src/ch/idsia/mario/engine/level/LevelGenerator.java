package ch.idsia.mario.engine.level;

import ch.idsia.mario.engine.sprites.Enemy;

import java.util.Random;


public class LevelGenerator
{
    public static final int TYPE_OVERGROUND = 0;
    public static final int TYPE_UNDERGROUND = 1;
    public static final int TYPE_CASTLE = 2;

    private static Random levelSeedRandom = new Random();
    public static long lastSeed;
    public static final int LevelLengthMinThreshold = 50;

    public static Level createLevel(int width, int height, long seed, int difficulty, int type)
    {
        LevelGenerator levelGenerator = new LevelGenerator(width, height);
        return levelGenerator.createLevel(seed, difficulty, type);
    }

    // Implementaci�n ORIGINAL.
    private int width;
    private int height;
    Level level = new Level(width, height);
    Random random;

    private static final int ODDS_STRAIGHT = 0;
    private static final int ODDS_HILL_STRAIGHT = 1;
    private static final int ODDS_TUBES = 2;
    private static final int ODDS_JUMP = 3;
    private static final int ODDS_CANNONS = 4;
    private int[] odds = new int[5];
    private int totalOdds;
    private int difficulty;
    private int type;

    // Implementaci�n GEN�TICA.
    // Poblaci�n: 50 individuos.
    private int maxPopulation = 50;                                 // Poblaci�n o n�mero de individuos.
    private int maxElementsPerLevel = 30;                           // Cromosomas.
    private int maxArgsPerElement = 6;                              // Genes.
    private int maxIterations = 10000;                              // N�mero m�ximo de iteraciones del proceso evolutivo.

    // Esto estaba antes en la clase como private, pero quiero probar a hacer vectores est�ticos, mucho m�s eficiente (creo). Reservamos espacio suficiente.
    // Fenotipo: Ser� una colecci�n de arrays (cromosomas) de arrays de flotantes (genes).
    private int[][][] phenotype = new int[maxPopulation][maxElementsPerLevel][maxArgsPerElement];



    private LevelGenerator(int width, int height)
    {
        this.width = width;
        this.height = height;
    }
/*
    // Funci�n de generaci�n de nivel ORIGINAL.
    private Level createLevel(long seed, int difficulty, int type)
    {
        // Se fijan el tipo, dificultad y posibilidades iniciales seg�n �sta �ltima.
        this.type = type;
        this.difficulty = difficulty;
        odds[ODDS_STRAIGHT] = 20;
        odds[ODDS_HILL_STRAIGHT] = 10;
        odds[ODDS_TUBES] = 2 + 1 * difficulty;
        odds[ODDS_JUMP] = 2 * difficulty;
        odds[ODDS_CANNONS] = -10 + 5 * difficulty;

        // Si el tipo del nivel no es de suelo, las posibilidades de colina se ponen a 0.
        if (type != LevelGenerator.TYPE_OVERGROUND)
        {
            odds[ODDS_HILL_STRAIGHT] = 0;
        }

        // En odds[i] se guarda un acumulado hasta el momento (hasta el �ndice i). Adem�s se ponen todas las posibilidades negativas a 0.
        for (int i = 0; i < odds.length; i++)
        {
            if (odds[i] < 0) odds[i] = 0;
            totalOdds += odds[i];
            odds[i] = totalOdds - odds[i];
        }

        // Se reserva memoria para el mapa (array de bytes).
        lastSeed = seed;
        level = new Level(width, height);
        random = new Random(seed);

        // Se construye una zona planaa inicial (buildStraight).
        int length = 0;
        length += buildStraight(0, level.width, true);

        // Se dibuja el resto de bloques de forma aleatoria (nextInt), sumando la anchura de cada uno en la variable length.
        // Se dejan 64 bloques supongo que para el final del nivel.

        // TOCAR AQU� PARA CAMBIAR EL NIVEL. SI COMENTAMOS ESTE BLOQUE S�LO SE GENERA EL FINAL DEL MISMO.
        while (length < level.width - 64)
        {
            length += buildZone(length, level.width - length);
        }

        int floor = height - 1 - random.nextInt(4);

        level.xExit = length + 8;
        level.yExit = floor;

        // �Cierra el nivel con un muro?
        for (int x = length; x < level.width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                if (y >= floor)
                {
                    level.setBlock(x, y, (byte) (1 + 9 * 16));
                }
            }
        }


        if (type == LevelGenerator.TYPE_CASTLE || type == LevelGenerator.TYPE_UNDERGROUND)
        {
            int ceiling = 0;
            int run = 0;
            for (int x = 0; x < level.width; x++)
            {
                if (run-- <= 0 && x > 4)
                {
                    ceiling = random.nextInt(4);
                    run = random.nextInt(4) + 4;
                }
                for (int y = 0; y < level.height; y++)
                {
                    if ((x > 4 && y <= ceiling) || x < 1)
                    {
                        level.setBlock(x, y, (byte) (1 + 9 * 16));
                    }
                }
            }
        }

        fixWalls();

        // Incluir algoritmo gen�tico:
        //  1. �C�mo codificar lo que queremos para la funci�n fitness?
        //  2. �C�mo generar los componentes a partir de lo que hemos codificado?
        //  3. Controlar que el nivel sea finalizable, alturas correctas y sin obst�culos insalvables.
        return level;
    }
*/
    // Inicializaci�n de la poblaci�n: Aleatorio.         >> Investigar m�todos <<
    // Debemos incluir las restricciones aqu� -> ������� El nivel debe poder finalizarse y los elementos no sobrepasar�n el ancho y alto del mismo !!!!!!
    private void initializePopulation () {

        int x, y, wg, h, w, wc, wbefore, wafter, levelElement;
        for (int i=0; i<maxPopulation; i++) {
            for (int j = 0; j < maxElementsPerLevel/*COMPROBACION ANCHO*/; j++) {

                // Para cada posible elemento
                levelElement = levelSeedRandom.nextInt(15) + 1;                       // levelElement -> Generamos un aleatorio entre 1 y 14.
                x = levelSeedRandom.nextInt(91) + 5;                                  // x -> Generamos un aleatorio entre 5 y 95.
                y = levelSeedRandom.nextInt(3) + 3;                                   // y -> Generamos un aleatorio entre 3 y 5.

                // Rellenamos los cromosomas de cada individuo de la poblaci�n de forma aleatoria.
                // Diferenciamos casos seg�n el tipo de elemento generado.
                switch (levelElement) {
                    case 1:
                        // gap.
                        wg = levelSeedRandom.nextInt(3) + 1;                           // wg -> Generamos un aleatorio entre 1 y 3.
                        wbefore = levelSeedRandom.nextInt(4) + 1;                      // wbefore -> Generamos un aleatorio entre 1 y 4.
                        wafter = levelSeedRandom.nextInt(4) + 1;                       // wafter -> Generamos un aleatorio entre 1 y 4.

                        // Rellenamos el cromosoma con los genes obtenidos.
                        phenotype[i][j][0] = levelElement;
                        phenotype[i][j][1] = x;
                        phenotype[i][j][2] = y;
                        phenotype[i][j][3] = wg;
                        phenotype[i][j][4] = wbefore;
                        phenotype[i][j][5] = wafter;

                        break;
                    case 4:
                    case 5:
                    case 7:
                    case 8:
                        // cannon_hill, tube_hill, cannon y tube.
                        // Todos estos elementos (ver gram�tica en el folio) tienen 5 par�metros (x, y, h � wg, wbefore, wafter).
                        // La gram�tica no especifica los valores que toman h, w, wg y dem�s, deberemos ir probando.
                        h = levelSeedRandom.nextInt(5) + 1;                            // h -> Generamos un aleatorio entre 1 y 5.

                        wbefore = levelSeedRandom.nextInt(4) + 1;                      // wbefore -> Generamos un aleatorio entre 1 y 4.
                        wafter = levelSeedRandom.nextInt(4) + 1;                       // wafter -> Generamos un aleatorio entre 1 y 4.

                        // Rellenamos el cromosoma con los genes obtenidos.
                        phenotype[i][j][0] = levelElement;
                        phenotype[i][j][1] = x;
                        phenotype[i][j][2] = y;
                        phenotype[i][j][3] = h;
                        phenotype[i][j][4] = wbefore;
                        phenotype[i][j][5] = wafter;

                        break;
                    case 2:
                    case 3:
                        // platform y hill.
                        w = levelSeedRandom.nextInt(8) + 1;                           // w -> Generamos un aleatorio entre 1 y 8.

                        phenotype[i][j][0] = levelElement;
                        phenotype[i][j][1] = x;
                        phenotype[i][j][2] = y;
                        phenotype[i][j][3] = w;

                        break;
                    case 6:
                        // coin.

                        wc = levelSeedRandom.nextInt(4) + 1;                           // wc -> Generamos un aleatorio entre 1 y 4.

                        phenotype[i][j][0] = levelElement;
                        phenotype[i][j][1] = x;
                        phenotype[i][j][2] = y;
                        phenotype[i][j][3] = wc;

                        break;
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                        // Cajas: block_coin, block_powerup, rock_coin, rock_empty.
                        phenotype[i][j][0] = levelElement;
                        phenotype[i][j][1] = x;
                        phenotype[i][j][2] = y;
                }
            }
        }
        // --> �Incluir aqu� la plataforma del final de nivel? <--
        // Lo de los goompas y koopas creo que hay que a�adirlos en las plataformas directamente. En la gram�tica aparece como un proceso separado.
    }


    // Evaluaci�n: Tomar� la poblaci�n en el momento en que se llama y devolver� el valor fitness de cada individuo.
    private float[] evaluate(float[] fitnessValues) {

        /* TODO */

        return fitnessValues;
    }

    // Cruce: Se reciben los dos padres (provisional) y a partir de ellos se obtiene un hijo.
    private int[][] crossOperator (int [][] firsParent, int [][] secondParent) {

        int [][] child = null;

        /* TODO */

        return child;
    }

    // Mutaci�n: Tomar� aleatoriamente miembros de la poblaci�n y realizar� peque�as modificaciones en ellos.
    private void mutation () {

        /* TODO */

    }

    // Reemplazamiento: Del hijo o hijos obtenidos se reemplazan los elementos de la poblaci�n actual seg�n un esquema que debemos decidir.
    private void populationReplacement (int [][] child, float [] fitnessValues) {

        /* TODO */

    }

    // Funci�n de generaci�n de nivel GEN�TICO.
    private Level createLevel(long seed, int difficulty, int type) {

        // Array de valores fitness. Al declararlo as�, fitness values tendr� una direcci�n de memoria, haciendo que el paso de par�metros sea por referencia.
        float [] fitnessValues = new float [maxPopulation];

        // N�mero de iteraciones del proceso evolutivo. Debe ser menor que maxIterations.
        int numIterations = 0;
        int tournamentIterations = 0;
        int [] selectedParents = new int[2];
        int [][] child;

        // Inicializamos el generador con la semilla.
        levelSeedRandom.setSeed(seed);

        // 0. Por defecto todos los valores de todos los genes de todos los cromosomas se inicializan a -1.
        // Servir� para decidir hasta cu�ndo tomar los argumentos de los elementos del nivel cuando sea necesario.
        for (int i=0; i<maxPopulation; i++)
            for (int j=0; j<maxElementsPerLevel; j++) {
                phenotype[i][j][0] = -1;
                phenotype[i][j][1] = -1;
                phenotype[i][j][2] = -1;
                phenotype[i][j][3] = -1;
                phenotype[i][j][4] = -1;
                phenotype[i][j][5] = -1;
            }

        // 0.1. Plataforma inicial: Cada cromosoma se inicializa con un primer gen que se corresponde con la plataforma inicial del juego.
        for (int i=0; i<maxPopulation; i++) {
            phenotype[i][0][0] = 0;                             // Tipo de elemento 0 (plataforma inicial).
            phenotype[i][0][1] = 0;                             // Coordenada x en el nivel.
            phenotype[i][0][2] = 5;                             // Coordenada y en el nivel.
            phenotype[i][0][3] = 10;                            // Anchura de la plataforma.
        }

        // 1. Inicializaci�n de la poblaci�n: Aleatoriamente.
        initializePopulation();



        // 2. Evaluaci�n inicial de la poblaci�n.
        evaluate(fitnessValues);

        // 3. Bucle principal, donde se realiza el proceso evolutivo.
        do {

            // 3.1. Selecci�n de padres: Torneo binario. Se elige el mejor de dos padres, se hace dos veces (binario).
            do {
                int firstParent = levelSeedRandom.nextInt(50);
                int secondParent = levelSeedRandom.nextInt(50);

                if (firstParent != secondParent) {
                    selectedParents[tournamentIterations] = fitnessValues[firstParent] < fitnessValues[secondParent] ? firstParent : secondParent;
                    tournamentIterations++;
                }

            } while (tournamentIterations < 2);

            // 3.2. Cruce de los dos padres ������ Hacer las comprobaciones necesarias para que el hijo resultante sea v�lido !!!!!!!
            child = crossOperator (phenotype[selectedParents[0]], phenotype[selectedParents[1]]);

            // 3.3 Mutaci�n.
            mutation();

            // 3.4. Reemplazamiento. Decidir.
            populationReplacement(child, fitnessValues);

            // 3.5. Evaluaci�n de la poblaci�n.
            evaluate (fitnessValues);

            numIterations++;
            tournamentIterations = 0;
        } while (numIterations < maxIterations);


        // Mostrar el mejor hijo (opcional)

        return level;
    }
    // Zona para crear partes del nivel.
    private int buildZone(int x, int maxLength)
    {

        // Se selecciona un componente aleatoriamente.
        int t = random.nextInt(totalOdds);
        int type = 0;
        for (int i = 0; i < odds.length; i++)
        {
            if (odds[i] <= t)
            {
                type = i;
            }
        }

        switch (type)
        {
            case ODDS_STRAIGHT:
                return buildStraight(x, maxLength, false);
            case ODDS_HILL_STRAIGHT:
                return buildHillStraight(x, maxLength);
            case ODDS_TUBES:
                return buildTubes(x, maxLength);
            case ODDS_JUMP:
                return buildJump(x, maxLength);
            case ODDS_CANNONS:
                return buildCannons(x, maxLength);
        }
        return 0;
    }

    // Se dibujan en el mapa partes del nivel. Cada m�todo devuelve la longitud de el bloque elegido.
    private int buildJump(int xo, int maxLength)
    {
        int js = random.nextInt(4) + 2;
        int jl = random.nextInt(2) + 2;
        int length = js * 2 + jl;

        boolean hasStairs = random.nextInt(3) == 0;

        int floor = height - 1 - random.nextInt(4);
        for (int x = xo; x < xo + length; x++)
        {
            if (x < xo + js || x > xo + length - js - 1)
            {
                for (int y = 0; y < height; y++)
                {
                    if (y >= floor)
                    {
                        level.setBlock(x, y, (byte) (1 + 9 * 16));
                    }
                    else if (hasStairs)
                    {
                        if (x < xo + js)
                        {
                            if (y >= floor - (x - xo) + 1)
                            {
                                level.setBlock(x, y, (byte) (9 + 0 * 16));
                            }
                        }
                        else
                        {
                            if (y >= floor - ((xo + length) - x) + 2)
                            {
                                level.setBlock(x, y, (byte) (9 + 0 * 16));
                            }
                        }
                    }
                }
            }
        }

        return length;
    }

    private int buildCannons(int xo, int maxLength)
    {
        int length = random.nextInt(10) + 2;
        if (length > maxLength) length = maxLength;

        int floor = height - 1 - random.nextInt(4);
        int xCannon = xo + 1 + random.nextInt(4);
        for (int x = xo; x < xo + length; x++)
        {
            if (x > xCannon)
            {
                xCannon += 2 + random.nextInt(4);
            }
            if (xCannon == xo + length - 1) xCannon += 10;
            int cannonHeight = floor - random.nextInt(4) - 1;

            for (int y = 0; y < height; y++)
            {
                if (y >= floor)
                {
                    level.setBlock(x, y, (byte) (1 + 9 * 16));
                }
                else
                {
                    if (x == xCannon && y >= cannonHeight)
                    {
                        if (y == cannonHeight)
                        {
                            level.setBlock(x, y, (byte) (14 + 0 * 16));
                        }
                        else if (y == cannonHeight + 1)
                        {
                            level.setBlock(x, y, (byte) (14 + 1 * 16));
                        }
                        else
                        {
                            level.setBlock(x, y, (byte) (14 + 2 * 16));
                        }
                    }
                }
            }
        }

        return length;
    }

    private int buildHillStraight(int xo, int maxLength)
    {
        int length = random.nextInt(10) + 10;
        if (length > maxLength) length = maxLength;

        int floor = height - 1 - random.nextInt(4);
        for (int x = xo; x < xo + length; x++)
        {
            for (int y = 0; y < height; y++)
            {
                if (y >= floor)
                {
                    level.setBlock(x, y, (byte) (1 + 9 * 16));
                }
            }
        }

        addEnemyLine(xo + 1, xo + length - 1, floor - 1);

        int h = floor;

        boolean keepGoing = true;

        boolean[] occupied = new boolean[length];
        while (keepGoing)
        {
            h = h - 2 - random.nextInt(3);

            if (h <= 0)
            {
                keepGoing = false;
            }
            else
            {
                int l = random.nextInt(5) + 3;
                int xxo = random.nextInt(length - l - 2) + xo + 1;

                if (occupied[xxo - xo] || occupied[xxo - xo + l] || occupied[xxo - xo - 1] || occupied[xxo - xo + l + 1])
                {
                    keepGoing = false;
                }
                else
                {
                    occupied[xxo - xo] = true;
                    occupied[xxo - xo + l] = true;
                    addEnemyLine(xxo, xxo + l, h - 1);
                    if (random.nextInt(4) == 0)
                    {
                        decorate(xxo - 1, xxo + l + 1, h);
                        keepGoing = false;
                    }
                    for (int x = xxo; x < xxo + l; x++)
                    {
                        for (int y = h; y < floor; y++)
                        {
                            int xx = 5;
                            if (x == xxo) xx = 4;
                            if (x == xxo + l - 1) xx = 6;
                            int yy = 9;
                            if (y == h) yy = 8;

                            if (level.getBlock(x, y) == 0)
                            {
                                level.setBlock(x, y, (byte) (xx + yy * 16));
                            }
                            else
                            {
                                if (level.getBlock(x, y) == (byte) (4 + 8 * 16)) level.setBlock(x, y, (byte) (4 + 11 * 16));
                                if (level.getBlock(x, y) == (byte) (6 + 8 * 16)) level.setBlock(x, y, (byte) (6 + 11 * 16));
                            }
                        }
                    }
                }
            }
        }

        return length;
    }

    private void addEnemyLine(int x0, int x1, int y)
    {
        for (int x = x0; x < x1; x++)
        {
            if (random.nextInt(35) < difficulty + 1)
            {
                int type = random.nextInt(4);
                if (difficulty < 1)
                {
                    type = Enemy.ENEMY_GOOMBA;
                }
                else if (difficulty < 3)
                {
                    type = random.nextInt(3);
                }
                level.setSpriteTemplate(x, y, new SpriteTemplate(type, random.nextInt(35) < difficulty));
            }
        }
    }

    private int buildTubes(int xo, int maxLength)
    {
        int length = random.nextInt(10) + 5;
        if (length > maxLength) length = maxLength;

        int floor = height - 1 - random.nextInt(4);
        int xTube = xo + 1 + random.nextInt(4);
        int tubeHeight = floor - random.nextInt(2) - 2;
        for (int x = xo; x < xo + length; x++)
        {
            if (x > xTube + 1)
            {
                xTube += 3 + random.nextInt(4);
                tubeHeight = floor - random.nextInt(2) - 2;
            }
            if (xTube >= xo + length - 2) xTube += 10;

            if (x == xTube && random.nextInt(11) < difficulty + 1)
            {
                level.setSpriteTemplate(x, tubeHeight, new SpriteTemplate(Enemy.ENEMY_FLOWER, false));
            }

            for (int y = 0; y < height; y++)
            {
                if (y >= floor)
                {
                    level.setBlock(x, y, (byte) (1 + 9 * 16));
                }
                else
                {
                    if ((x == xTube || x == xTube + 1) && y >= tubeHeight)
                    {
                        int xPic = 10 + x - xTube;
                        if (y == tubeHeight)
                        {
                            level.setBlock(x, y, (byte) (xPic + 0 * 16));
                        }
                        else
                        {
                            level.setBlock(x, y, (byte) (xPic + 1 * 16));
                        }
                    }
                }
            }
        }

        return length;
    }

    private int buildStraight(int xo, int maxLength, boolean safe)
    {
        int length = random.nextInt(10) + 2;
        if (safe) length = 10 + random.nextInt(5);
        if (length > maxLength) length = maxLength;

        int floor = height - 1 - random.nextInt(4);
        for (int x = xo; x < xo + length; x++)
        {
            for (int y = 0; y < height; y++)
            {
                if (y >= floor)
                {
                    level.setBlock(x, y, (byte) (1 + 9 * 16));
                }
            }
        }

        if (!safe)
        {
            if (length > 5)
            {
                decorate(xo, xo + length, floor);
            }
        }

        return length;
    }

    private void decorate(int x0, int x1, int floor)
    {
        if (floor < 1) return;

        //        boolean coins = random.nextInt(3) == 0;
        boolean rocks = true;

        addEnemyLine(x0 + 1, x1 - 1, floor - 1);

        int s = random.nextInt(4);
        int e = random.nextInt(4);

        if (floor - 2 > 0)
        {
            if ((x1 - 1 - e) - (x0 + 1 + s) > 1)
            {
                for (int x = x0 + 1 + s; x < x1 - 1 - e; x++)
                {
                    level.setBlock(x, floor - 2, (byte) (2 + 2 * 16));
                }
            }
        }

        s = random.nextInt(4);
        e = random.nextInt(4);

        if (floor - 4 > 0)
        {
            if ((x1 - 1 - e) - (x0 + 1 + s) > 2)
            {
                for (int x = x0 + 1 + s; x < x1 - 1 - e; x++)
                {
                    if (rocks)
                    {
                        if (x != x0 + 1 && x != x1 - 2 && random.nextInt(3) == 0)
                        {
                            if (random.nextInt(4) == 0)
                            {
                                level.setBlock(x, floor - 4, (byte) (4 + 2 + 1 * 16));
                            }
                            else
                            {
                                level.setBlock(x, floor - 4, (byte) (4 + 1 + 1 * 16));
                            }
                        }
                        else if (random.nextInt(4) == 0)
                        {
                            if (random.nextInt(4) == 0)
                            {
                                level.setBlock(x, floor - 4, (byte) (2 + 1 * 16));
                            }
                            else
                            {
                                level.setBlock(x, floor - 4, (byte) (1 + 1 * 16));
                            }
                        }
                        else
                        {
                            level.setBlock(x, floor - 4, (byte) (0 + 1 * 16));
                        }
                    }
                }
            }
        }

        int length = x1 - x0 - 2;

        /*        if (length > 5 && rocks)
         {
         decorate(x0, x1, floor - 4);
         }*/
    }

    private void fixWalls()
    {
        boolean[][] blockMap = new boolean[width + 1][height + 1];
        for (int x = 0; x < width + 1; x++)
        {
            for (int y = 0; y < height + 1; y++)
            {
                int blocks = 0;
                for (int xx = x - 1; xx < x + 1; xx++)
                {
                    for (int yy = y - 1; yy < y + 1; yy++)
                    {
                        if (level.getBlockCapped(xx, yy) == (byte) (1 + 9 * 16)) blocks++;
                    }
                }
                blockMap[x][y] = blocks == 4;
            }
        }
        blockify(level, blockMap, width + 1, height + 1);
    }

    private void blockify(Level level, boolean[][] blocks, int width, int height)
    {
        int to = 0;
        if (type == LevelGenerator.TYPE_CASTLE)
        {
            to = 4 * 2;
        }
        else if (type == LevelGenerator.TYPE_UNDERGROUND)
        {
            to = 4 * 3;
        }

        boolean[][] b = new boolean[2][2];
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                for (int xx = x; xx <= x + 1; xx++)
                {
                    for (int yy = y; yy <= y + 1; yy++)
                    {
                        int _xx = xx;
                        int _yy = yy;
                        if (_xx < 0) _xx = 0;
                        if (_yy < 0) _yy = 0;
                        if (_xx > width - 1) _xx = width - 1;
                        if (_yy > height - 1) _yy = height - 1;
                        b[xx - x][yy - y] = blocks[_xx][_yy];
                    }
                }

                if (b[0][0] == b[1][0] && b[0][1] == b[1][1])
                {
                    if (b[0][0] == b[0][1])
                    {
                        if (b[0][0])
                        {
                            level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
                        }
                        else
                        {
                            // KEEP OLD BLOCK!
                        }
                    }
                    else
                    {
                        if (b[0][0])
                        {
                            level.setBlock(x, y, (byte) (1 + 10 * 16 + to));
                        }
                        else
                        {
                            level.setBlock(x, y, (byte) (1 + 8 * 16 + to));
                        }
                    }
                }
                else if (b[0][0] == b[0][1] && b[1][0] == b[1][1])
                {
                    if (b[0][0])
                    {
                        level.setBlock(x, y, (byte) (2 + 9 * 16 + to));
                    }
                    else
                    {
                        level.setBlock(x, y, (byte) (0 + 9 * 16 + to));
                    }
                }
                else if (b[0][0] == b[1][1] && b[0][1] == b[1][0])
                {
                    level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
                }
                else if (b[0][0] == b[1][0])
                {
                    if (b[0][0])
                    {
                        if (b[0][1])
                        {
                            level.setBlock(x, y, (byte) (3 + 10 * 16 + to));
                        }
                        else
                        {
                            level.setBlock(x, y, (byte) (3 + 11 * 16 + to));
                        }
                    }
                    else
                    {
                        if (b[0][1])
                        {
                            level.setBlock(x, y, (byte) (2 + 8 * 16 + to));
                        }
                        else
                        {
                            level.setBlock(x, y, (byte) (0 + 8 * 16 + to));
                        }
                    }
                }
                else if (b[0][1] == b[1][1])
                {
                    if (b[0][1])
                    {
                        if (b[0][0])
                        {
                            level.setBlock(x, y, (byte) (3 + 9 * 16 + to));
                        }
                        else
                        {
                            level.setBlock(x, y, (byte) (3 + 8 * 16 + to));
                        }
                    }
                    else
                    {
                        if (b[0][0])
                        {
                            level.setBlock(x, y, (byte) (2 + 10 * 16 + to));
                        }
                        else
                        {
                            level.setBlock(x, y, (byte) (0 + 10 * 16 + to));
                        }
                    }
                }
                else
                {
                    level.setBlock(x, y, (byte) (0 + 1 * 16 + to));
                }
            }
        }
    }
}