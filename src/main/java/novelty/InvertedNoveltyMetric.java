package novelty;

import emo.Individual;

import java.util.List;

public class InvertedNoveltyMetric {

    public static List<Individual> neighbours;

    public static void main(String[] args) {
        Individual individual = null;
    }

    public static double calcSparseness(Individual solution) {
        if(neighbours == null)
            return 0;
        double summationOfDistancesToNeighbours = 0;
        for (int i = 0; i < neighbours.size(); i++) {
            Individual neighbour = neighbours.get(i);
            summationOfDistancesToNeighbours += getEuclideanDistance(solution, neighbour);
        }
        return summationOfDistancesToNeighbours / neighbours.size() * -1;
    }

    private static double getEuclideanDistance(Individual solution1, Individual solution2) {
        double squareSum = 0;
        for (int i = 0; i < solution1.real.length; i++) {
            squareSum += Math.pow(solution1.real[i] - solution2.real[i], 2);
        }
        return Math.sqrt(squareSum);
    }
}
