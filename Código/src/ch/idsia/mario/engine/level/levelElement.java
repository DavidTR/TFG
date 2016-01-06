package ch.idsia.mario.engine.level;

/**
 * Created by roka9 on 18/10/2015.
 */
public class LevelElement {
    int elementType = -1;
    int x = -1;
    int y = -1;
    int param1 = -1, param2 = -1, param3 = 1;

    public LevelElement() {
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
