package ch.idsia.mario.engine.level;

import java.util.ArrayList;

/**
 * Created by roka9 on 18/10/2015.
 */
public class Individual {
    static private int maxElementsPerLevel = 30;                           // Cromosomas.

    ArrayList<LevelElement> individual = new ArrayList<LevelElement>(maxElementsPerLevel);

    // En esta estructura guardamos las posiciones de los elementos sobre los que el algoritmo genetico puede actuar.
    private ArrayList<Integer> geneticElements = new ArrayList<Integer>(maxElementsPerLevel);

    public Individual() {
        for (int i=0; i<maxElementsPerLevel; i++)
            individual.add(new LevelElement());
    }

    public Individual (Individual ind) {
        this.setGeneticElements(ind.getGeneticElements());
        this.setIndividual(ind.getIndividual());
    }

    private void setIndividual (ArrayList<LevelElement> ind) {
        this.individual = new ArrayList<LevelElement>(ind);
    }

    public ArrayList<LevelElement> getIndividual () {
        return individual;
    }

    private void setGeneticElements (ArrayList<Integer> ge) {
        this.geneticElements = new ArrayList<Integer>(ge);
    }

    public ArrayList<Integer> getGeneticElements() {
        return geneticElements;
    }

    public void addGeneticElement(Integer elem) {
        geneticElements.add(elem);
    }

    public LevelElement getElement (int position) {
        return individual.get(position);
    }

    public void setElementParam(int position, int param, int value) {
        LevelElement elem = new LevelElement();

        LevelElement values = individual.get(position);

        elem.setElementType(values.getElementType());
        elem.setX(values.getX());
        elem.setY(values.getY());

        switch (param) {
            case 1:
                elem.setParam1(value);
                elem.setParam2(values.getParam2());
                elem.setParam3(values.getParam3());
                break;

            case 2:
                elem.setParam1(values.getParam1());
                elem.setParam2(value);
                elem.setParam3(values.getParam3());
                break;

            case 3:
                elem.setParam1(values.getParam1());
                elem.setParam2(values.getParam2());
                elem.setParam3(value);
                break;
        }

        individual.set(position, elem);
    }
}

