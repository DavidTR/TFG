package ch.idsia.mario.engine.third_solution;

import ch.idsia.mario.engine.level.*;

import java.util.ArrayList;

/**
 * Created by roka9 on 18/10/2015.
 *
 * Clase Individual: Codificacion de cada individuo.
 */
public class IndividualThirdSolution {
    static private int maxElementsPerLevel = 30;                                                                        // Cromosomas.

    private ArrayList<LevelElementThirdSolution> individual = new ArrayList<LevelElementThirdSolution>(maxElementsPerLevel);

    // En esta estructura guardamos las posiciones de los elementos sobre los que el algoritmo genetico puede actuar.
    private ArrayList<Integer> geneticElements = new ArrayList<Integer>(maxElementsPerLevel);

    public IndividualThirdSolution() {
        for (int i=0; i<maxElementsPerLevel; i++)
            individual.add(new LevelElementThirdSolution());
    }

    public IndividualThirdSolution (IndividualThirdSolution ind) {
        this.geneticElements = new ArrayList<Integer>(ind.getGeneticElements());
        this.individual = new ArrayList<LevelElementThirdSolution>(ind.getIndividual());
    }

    ArrayList<LevelElementThirdSolution> getIndividual () {
        return individual;
    }

    ArrayList<Integer> getGeneticElements() {
        return geneticElements;
    }

    void addGeneticElement(Integer elem) {
        geneticElements.add(elem);
    }

    void deleteGeneticElement (int position) { geneticElements.remove(position); }

    LevelElementThirdSolution getElement (int position) {
        return individual.get(position);
    }

    void setElementParam(int position, int param, int value) {
        LevelElementThirdSolution elem = new LevelElementThirdSolution();

        LevelElementThirdSolution values = individual.get(position);

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

        for (LevelElementThirdSolution element : this.getIndividual()){

            if (element.getElementType() != -1) {
                accumulate += element.getElementType();

                if (element.getElementType() == GeneticLevelGeneratorThirdSolution.GAP)
                    if (element.getParam1() < 4)
                        accumulate += element.getParam1();
                    else
                        accumulate += element.getParam1()*2;

                totalElements++;
            }
            else
                break;
        }

        return ((float) accumulate/totalElements);
    }
}

