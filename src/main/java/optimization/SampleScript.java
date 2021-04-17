/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import emo.DoubleAssignmentException;
import emo.Individual;
import emo.OptimizationProblem;
import engines.NSGA3Engine;
import engines.UnifiedNsga3Engine;
import evaluators.ZDT1Evaluator;
import novelty.ZDT1WithNoveltyEvaluator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import parsing.IndividualEvaluator;
import parsing.InvalidOptimizationProblemException;
import parsing.StaXParser;
import utils.PerformanceMetrics;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is a sample class showing the simplest way to use EvoLib
 *
 * @author Haitham Seada
 */
public class SampleScript extends TestScript {

    public static DescriptiveStatistics winnerGenerationIndicesStats = new DescriptiveStatistics();

    public static void main(String[] args)
            throws XMLStreamException,
            InvalidOptimizationProblemException,
            IOException,
            FileNotFoundException ,
            DoubleAssignmentException {
        // Read problem
        URL url = SampleScript.class.getClassLoader().getResource( "samples/zdt1-02-30-constrained.xml");
        InputStream in = url.openStream();

        OptimizationProblem optimizationProblem = StaXParser.readProblem(in);

        // Create Evaluators
        //IndividualEvaluator individualEvaluator = new ZDT1Evaluator(); // Modify this line to switch between novelty and non-novelty
        IndividualEvaluator individualEvaluator = new ZDT1WithNoveltyEvaluator(); // Modify this line to switch between novelty and non-novelty
        IndividualEvaluator individualEvaluatorWithoutNovelty = new ZDT1Evaluator();
        // Throw an error with anything other than 2 objectives
        if(optimizationProblem.objectives.length != 2) {
            throw new UnsupportedOperationException(
                    "This code works only for 2 objectives. Step size calculations for 3+ objectives is different");
        }
        // Get the approximate Pareto front
        NSGA3Engine.TRACK_WINNER_GENERATION = false;
        Individual[] paretoFront = getApproximateParetoFront(
                optimizationProblem,
                individualEvaluatorWithoutNovelty,
                8,
                7,
                2); // Notice that the step size for 3 objective problems is NOT (popSize - 1)
        int trialsCount = 2;
        List<Double> igdList = new ArrayList<>();
        NSGA3Engine.TRACK_WINNER_GENERATION = true;

        NSGA3Engine.NOVELTY = true; // CHANGE THIS LINE TO SWITH BETWEEN STANDARD AND NOVELTY

        // Create Engine
        NSGA3Engine geneticEngine = new UnifiedNsga3Engine(optimizationProblem,individualEvaluator);
        // Specify output directory
        File outDir = new File(System.getProperty("user.home") + "/evolib_output40/standard");
        outDir.mkdirs();
        for(int i = 0; i < trialsCount; i++) {
            System.out.println("---------");
            // Optimize
            Individual[] finalPopulation
                    = geneticEngine.start(
                    outDir,
                    0,
                    0,
                    Double.MAX_VALUE,
                    Integer.MAX_VALUE);
            // Correct the objective values of the final population if needed (remove novelty if it exists)
            for (Individual individual : finalPopulation) {
                individualEvaluatorWithoutNovelty.updateIndividualObjectivesAndConstraints(optimizationProblem, individual);
            }
            // Calculate IGD
            double igd = PerformanceMetrics.calculateInvertedGenerationalDistance(
                    optimizationProblem.objectives.length,
                    finalPopulation,
                    paretoFront,
                    2);
            // Add IGD to the list
            igdList.add(igd);

//            // Display
//            display(finalPopulation);
        }
        // Display the list of IGDs
        System.out.println("IGD");
        for (Double igd : igdList) {
            System.out.println(igd);
        }
        // Display the list of winner generation indices
        System.out.println(String.format("%s (total attempts = %d) (found %d times) (mean index = %f)",
                Arrays.toString(winnerGenerationIndicesStats.getValues()),
                trialsCount,
                winnerGenerationIndicesStats.getValues().length,
                winnerGenerationIndicesStats.getMean()));
    }

    private static Individual[] getApproximateParetoFront(
            OptimizationProblem optimizationProblem,
            IndividualEvaluator individualEvaluatorWithoutNovelty,
            int individualCount,
            int steps,
            int generationsCount) {
        int originalPopSize = optimizationProblem.getPopulationSize();
        int originalSteps = optimizationProblem.getSteps();
        int originalGenerationsCount = optimizationProblem.getGenerationsCount();
        optimizationProblem.setPopulationSize(individualCount);
        optimizationProblem.setSteps(steps);
        optimizationProblem.setGenerationsCount(generationsCount);
        UnifiedNsga3Engine unifiedNsga3Engine = new UnifiedNsga3Engine(
                optimizationProblem,
                individualEvaluatorWithoutNovelty);
        // Specify output directory
        File outDir = new File(System.getProperty("user.home") + "/temp");
        outDir.mkdirs();
        // Optimize
        Individual[] approximateParetoFront = null;
        try {
            approximateParetoFront = unifiedNsga3Engine.start(
                    outDir,
                    0,
                    0,
                    Double.MAX_VALUE,
                    Integer.MAX_VALUE);
        } catch (DoubleAssignmentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Reset parameters
        optimizationProblem.setPopulationSize(originalPopSize);
        optimizationProblem.setSteps(originalSteps);
        optimizationProblem.setGenerationsCount(originalGenerationsCount);
        return approximateParetoFront;
    }

    /**
     * Display all population members.
     *
     * @param population a population of individuals
     */
    private static void display(Individual[] population) {
        System.out.println();
        System.out.println("Results:");
        System.out.println();
        for (Individual individual : population) {
            System.out.println(individual.toString());
        }
    }
}
