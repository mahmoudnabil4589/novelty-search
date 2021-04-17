/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package novelty;

import emo.Individual;
import emo.OptimizationProblem;
import parsing.IndividualEvaluator;

/**
 * ZDT1 Test Problem
 * @author toshiba
 */
public class ZDT1WithNoveltyEvaluator extends IndividualEvaluator {

    @Override
    public void updateIndividualObjectivesAndConstraints(
            OptimizationProblem problem,
            Individual individual) {
        double[] x = individual.real;
        double sum = 0;
        for (int i = 1; i < x.length; i++) {
            sum += x[i];
        }
        double d = 1.0 + 9.0 / (x.length - 1) * sum;
        double sparseness = InvertedNoveltyMetric.calcSparseness(individual);
        // 2 obj

        individual.setObjective(0, x[0] + sparseness);
        individual.setObjective(1, d * (1.0 - Math.sqrt(x[0] / d)) + sparseness);

        //3 obj
/*
        individual.setObjective(0, x[0]);
        individual.setObjective(1, d * (1.0 - Math.sqrt(x[0] / d)));
        individual.setObjective(2, InvertedNoveltyMetric.calcSparseness(individual));
*/


        // Announce that objective function values are valid1
        individual.validObjectiveFunctionsValues = true;
        // Update constraint violations if constraints exist
        if (problem.constraints != null) {
            // Four constraints
            /*
            double[] g = new double[6];
            g[0] = x[0] ;
            g[1] = (-1*x[0]+0.4)/0.4;
            g[2] =( x[0]-0.49) / 0.49;
            g[3] = (-1*x[0]+0.51) / 0.51;
            g[4] = (x[0]-0.60) / 0.60;
            g[5] = -1*x[0]+1.0;
            */
            double[] g = new double[4];
            g[0]=x[0];
            g[1]=(-1*x[0]+0.5)/0.5;
            g[2]=(x[0]-0.9)/0.9;
            g[3]=(-1*x[0]+0.901)/0.901;

            // Set constraints vilations
            for (int i = 0; i < g.length; i++) {
                if (g[i] < 0) {
                    individual.setConstraintViolation(i, g[i]);
                } else {
                    individual.setConstraintViolation(i, 0);
                }
            }

            // Announce that objective function values are valid
            individual.validConstraintsViolationValues = true;
        }
        // Increase Evaluations Count by One (counted per individual)
        funEvaCount++;
    }

    @Override
    public double[] getReferencePoint() {
        double[] refPoint = {137.037, 50};
        return refPoint;
    }

    @Override
    public double[] getIdealPoint() {
        //double[] refPoint = {0, 3.667};
        double[] refPoint = {-0.05, -0.05};
        return refPoint;
    }
}


