package ch.idsia.mario.engine.level;

/**
 * Created by roka9 on 18/10/2015.
 *
 * Clase LevelElement: Codificacion de cada elemento de un individuo.
 */
public class LevelElement {
    private int elementType = -1;
    private int x = -1;
    private int y = -1;
    private int param1 = -1, param2 = -1, param3 = 1;

    public LevelElement() {
        elementType = x = y = param1 = param2 = param3 = -1;
    }

    int getElementType () {
        return elementType;
    }

    int getX () {
        return x;
    }

    int getY () {
        return y;
    }

    int getParam1 () {
        return param1;
    }

    int getParam2 () {
        return param2;
    }

    int getParam3 () {
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
