package ch.idsia.mario.engine.second_solution;

/**
 * Created by roka9 on 18/10/2015.
 *
 * Clase LevelElement: Codificacion de un elemento de un individuo.
 */
public class LevelElementSecondSolution {
    private int elementType = -1;
    private int x = -1;
    private int y = -1;
    private int param1 = -1, param2 = -1, param3 = 1;

    public LevelElementSecondSolution() {
        elementType = x = y = param1 = param2 = param3 = -1;
    }

    public int getElementType () {
        return elementType;
    }

    public int getX () {
        return x;
    }

    public int getY () {
        return y;
    }

    public int getParam1 () {
        return param1;
    }

    public int getParam2 () {
        return param2;
    }

    public int getParam3 () {
        return param3;
    }

    void setElementType (int element) {
        elementType = element;
    }

    void setX (int xArg) {
        x = xArg;
    }

    void setY (int yArg) {
        y = yArg;
    }

    void setParam1 (int p1) {
        param1 = p1;
    }

    void setParam2 (int p2) {
        param2 = p2;
    }

    void setParam3 (int p3) {
        param3 = p3;
    }
}
