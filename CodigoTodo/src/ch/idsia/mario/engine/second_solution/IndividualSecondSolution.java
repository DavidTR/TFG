package ch.idsia.mario.engine.second_solution;

import java.util.ArrayList;

/**
 * Created by roka9 on 18/10/2015.
 */
public class IndividualSecondSolution {
    static private int maxElementsPerLevel = 30;                                                                        // Cromosomas.

    private ArrayList<LevelElementSecondSolution> individual = new ArrayList<LevelElementSecondSolution>(maxElementsPerLevel);

    // En esta estructura guardamos las posiciones de los elementos sobre los que el algoritmo genetico puede actuar.
    private ArrayList<Integer> geneticElements = new ArrayList<Integer>(maxElementsPerLevel);

    public IndividualSecondSolution() {
        for (int i=0; i<maxElementsPerLevel; i++)
            individual.add(new LevelElementSecondSolution());
    }

    public IndividualSecondSolution (IndividualSecondSolution ind) {
        this.geneticElements = new ArrayList<Integer>(ind.getGeneticElements());
        this.individual = new ArrayList<LevelElementSecondSolution>(ind.getIndividual());
    }

    ArrayList<LevelElementSecondSolution> getIndividual () {
        return individual;
    }

    ArrayList<Integer> getGeneticElements() {
        return geneticElements;
    }

    void addGeneticElement(Integer elem) {
        geneticElements.add(elem);
    }

    void deleteGeneticElement (int position) { geneticElements.remove(position); }

    LevelElementSecondSolution getElement (int position) {
        return individual.get(position);
    }

    void setElementParam(int position, int param, int value) {
        LevelElementSecondSolution elem = new LevelElementSecondSolution();

        LevelElementSecondSolution values = individual.get(position);

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

    float getStructuralDifficulty() {

        int accumulate = 0, totalElements = 0;

        for (LevelElementSecondSolution element : this.getIndividual()){

            if (element.getElementType() != -1) {
                accumulate += element.getElementType();
                totalElements++;
            }
            else
                break;
        }

        return ((float) accumulate/totalElements);
    }
}

