package coursework;

import java.util.ArrayList;
import java.util.Collections;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Parameter;

import model.Fitness;
import model.Individual;
import model.LunarParameters.DataSet;
import model.NeuralNetwork;

/**
 * Implements a basic Evolutionary Algorithm to train a Neural Network
 * 
 * You Can Use This Class to implement your EA or implement your own class that extends {@link NeuralNetwork} 
 * 
 */
public class ExampleEvolutionaryAlgorithm extends NeuralNetwork {
	

	/**
	 * The Main Evolutionary Loop
	 */
	@Override
	public void run() {		
		//Initialise a population of Individuals with random weights
		population = initialise();

		//Record a copy of the best Individual in the population
		best = getBest();
		System.out.println("Best From Initialisation " + best);

		/**
		 * main EA processing loop
		 */		
		
		while (evaluations < Parameters.maxEvaluations) {

			/**
			 * this is a skeleton EA - you need to add the methods.
			 * You can also change the EA if you want 
			 * You must set the best Individual at the end of a run
			 * 
			 */
			Individual parent1 = null; 
			Individual parent2 = null;

			if(Parameters.selection == Parameters.selectionMethod.roulette)
			{
				parent1 = rouletteSelect(); 
				parent2 = rouletteSelect();
			}
			else if (Parameters.selection == Parameters.selectionMethod.tournament)
			{
				parent1 = tournamentSelect(Parameters.tournamentSize);
				parent2 = tournamentSelect(Parameters.tournamentSize);
			}
		
			ArrayList<Individual> children = new ArrayList<Individual>();
			
			if(Parameters.crossover == Parameters.crossoverMethod.singlePoint)
			{
				children = singlePointCrossover(parent1, parent2);
			}
			else
			{
				children = uniformCrossover(parent1, parent2);
			}
			
			//mutate the offspring
			bitStringMutate(children);
			
			// Evaluate the children
			evaluateIndividuals(children);			

			// Replace children in population
			replace(children);

			// check to see if the best has improved
			best = getBest();
			
			// Implemented in NN class. 
			outputStats();
			
			//Increment number of completed generations		
			evaluations++;
		}

		//save the trained network to disk
		saveNeuralNetwork();
	}

	

	/**
	 * Sets the fitness of the individuals passed as parameters (whole population)
	 * 
	 */
	private void evaluateIndividuals(ArrayList<Individual> individuals) {
		for (Individual individual : individuals) {
			individual.fitness = Fitness.evaluate(individual, this);
		}
	}


	/**
	 * Returns a copy of the best individual in the population
	 * 
	 */
	private Individual getBest() {
		best = null;;
		for (Individual individual : population) {
			if (best == null) {
				best = individual.copy();
			} else if (individual.fitness < best.fitness) {
				best = individual.copy();
			}
		}
		return best;
	}

	/**
	 * Generates a randomly initialised population
	 * 
	 */
	private ArrayList<Individual> initialise() {
		population = new ArrayList<>();
		for (int i = 0; i < Parameters.popSize; ++i) {
			//chromosome weights are initialised randomly in the constructor
			Individual individual = new Individual();
			population.add(individual);
		}
		evaluateIndividuals(population);
		return population;
	}

	/**
	 * Selection --
	 */
	private Individual rouletteSelect() {	
		//Implementation of Roulette Selection
		//Returns the selected index based on fitness	
		double sumFitness = 0.0;
		//Return the last value of the population when rounding error occurs
		int individualIndex = population.size() - 1;
		//Sorts the list
		Collections.sort(population);
		//Define the sum fitness of the population inverted for minimisation
		for(Individual individual: population)
		{
			sumFitness += (1-individual.fitness);
		}
		//Generate random value based on sum fitness
		double ranVal = Parameters.random.nextDouble() * sumFitness;
		for(int i = 0; i < population.size(); i++)
		{
			ranVal -= (1-population.get(i).fitness);
			if(ranVal <= 0) 
			{
				individualIndex = i;
			}	
		}
		return population.get(individualIndex).copy();
	}
	
	private Individual tournamentSelect(int tournamentSize)
	{
		ArrayList<Individual> tournament = new ArrayList<Individual>();
		for(int i = 0; i < tournamentSize; i++)
		{
			tournament.add(population.get(Parameters.random.nextInt(population.size())).copy());
		}
		Collections.sort(tournament);
		
		return tournament.get(0);
	}

	/**
	 * Crossover / Reproduction
	 */
	private ArrayList<Individual> singlePointCrossover(Individual parent1, Individual parent2) {
		ArrayList<Individual> children = new ArrayList<>();
		Individual firstChild = new Individual();
		Individual secondChild = new Individual();
		int totalGenes = parent1.chromosome.length;
		int crossoverPoint = Parameters.random.nextInt(totalGenes);

		for(int i = 0; i < crossoverPoint; i++)
		{
			firstChild.chromosome[i] = parent1.chromosome[i];
			secondChild.chromosome[i] = parent2.chromosome[i];
		}
		
		for(int i = crossoverPoint; i < totalGenes; i++)
		{
			firstChild.chromosome[i] = parent2.chromosome[i];
			secondChild.chromosome[i] = parent1.chromosome[i];
		}
		
		children.add(firstChild);
		children.add(secondChild);	
		return children;
	} 
	
	private ArrayList<Individual> uniformCrossover(Individual parent1, Individual parent2)
	{
		ArrayList<Individual> children = new ArrayList<>();
		Individual firstChild = new Individual();
		Individual secondChild = new Individual();
		int totalGenes = parent1.chromosome.length;
		int crossoverPoint = Parameters.random.nextInt(2);
		
		for(int i = 0; i < totalGenes; i++)
		{
			if (crossoverPoint < 1)
			{
				firstChild.chromosome[i] = parent1.chromosome[i];
				secondChild.chromosome[i] = parent2.chromosome[i];
			}
			else
			{
				firstChild.chromosome[i] = parent2.chromosome[i];
				secondChild.chromosome[i] = parent1.chromosome[i];
			}
			crossoverPoint = Parameters.random.nextInt(2);
		}
		
		children.add(firstChild);
		children.add(secondChild);
		
		return children;
	}
	
	/**
	 * Mutation
	 * 
	 * 
	 */
	private void bitStringMutate(ArrayList<Individual> individuals) {		
		for(Individual individual : individuals) {
			for (int i = 0; i < individual.chromosome.length; i++) {
				if (Parameters.random.nextDouble() < Parameters.mutateRate) {
					if (Parameters.random.nextBoolean()) {
						individual.chromosome[i] += (Parameters.mutateChange);
					} else {
						individual.chromosome[i] -= (Parameters.mutateChange);
					}
				}
			}
		}		
	}
	
	private void flipBitMutate(ArrayList<Individual> individuals)
	{
		for(Individual individual : individuals)
		{
			for(int i = 0; i < individual.chromosome.length; i++)
			{
				if(Parameters.random.nextDouble() < Parameters.mutateRate)
				{
					if(individual.chromosome[i] == 0)
					{
						individual.chromosome[i] = 1;
					}
					else
					{
						individual.chromosome[i] = 0;
					}
				}
			}
		}
	}

	/**
	 * 
	 * Sorts the weakest quarter of the population into a new list
	 * Randomly replaces N(number of children) of these individuals
	 * Only if the incoming children have a better fitness 
	 */
	private void replace(ArrayList<Individual> individuals) {
		Collections.sort(population);
		ArrayList<Individual> replacable = new ArrayList<Individual>();
		for(Individual individual : population) {
			if(individual.fitness/4 > population.get(0).fitness)
			{
				replacable.add(individual);
			}
		}		
		
		for(Individual individual : individuals)
		{
			Individual replaced = replacable.get(Parameters.random.nextInt(replacable.size()));
			if(individual.fitness > replaced.fitness)
			{
				population.set(population.indexOf(replaced), individual);
			}
		}
	}

	

	/**
	 * Returns the index of the worst member of the population
	 * @return
	 */
	private int getWorstIndex() {
		Individual worst = null;
		int idx = -1;
		for (int i = 0; i < population.size(); i++) {
			Individual individual = population.get(i);
			if (worst == null) {
				worst = individual;
				idx = i;
			} else if (individual.fitness > worst.fitness) {
				worst = individual;
				idx = i; 
			}
		}
		return idx;
	}	

	@Override
	public double activationFunction(double x) {
		if (x < -20.0) {
			return -1.0;
		} else if (x > 20.0) {
			return 1.0;
		}
		return Math.tanh(x);
	}
}