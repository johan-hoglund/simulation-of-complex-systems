// Runs the whole simulation.
// Run by repeated calls to the tick() method, which prints statistics and calls populationStep().
//
// population is a list of all Agents, a call to evaluatePopulation() runs all agents against
// each others, total scores for each agent is then available in Agent.totalScore.
// see populationStep() for details on how new populations are formed.
//
// Some 20 lines down the code, there are definitions for the scoring.
// Most settings, for example roundsPerGame and other game-related settings that are defined
// here are overwritten by GuiRunner, so no need to think about their values here.
//

import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import javax.swing.*;
import java.awt.*;

public class GameRunner implements Runnable
{
	// Simulation parameters
	public int roundsPerGame = 50;
	public int populationSize = 100; // Must be even!
	public static int fps = 0;
	
	// Scores
	public double mutualCooperationScore = 3;
	public double mutualDefectionScore = 1;
	public double loseScore = 0;
	public double winScore = 5;

	// Game parameters
	public double noise = 0;
	
	// Strategy parameters
	public double strategyMutationRate = 0.01;
	
	// Starting memory parameters
	public int startingMemoryMinSize = 1;
	public int startingMemoryMaxSize = 5;
	public double startingMemorySizeMutationRate = 0;
	public double startingMemoryMutationRate = 0;

	// Override parameters
	public double overrideMutationRate = 0.01;

	public int replacementCount = 1;

	public boolean keepRunning = true;

	public AgentComparator ac;

	JTextArea outputLog;


	public int roundNumber = 0;
	private ArrayList<Agent> population;

	// Methods
	public HashMap<String, Integer> populationHistogram()
	{
		HashMap<String, Integer> histogram = new HashMap<String, Integer>();

		for(int i = 0; i < populationSize; i++)
		{
			String genome = population.get(i).genomeString();
			Integer frequency;
			if((frequency = histogram.get(genome)) == null)
			{
				histogram.put(genome, new Integer(1));
			}
			else
			{
				histogram.put(genome, new Integer(frequency.intValue()+1));
			}
		}
		return histogram;
	}

	public void run()
	{
		keepRunning = true;
		while(true)
		{
			while(keepRunning)
			{
				tick();
			}
			while(!keepRunning)
			{
				try
				{
					Thread.sleep(1000);
				}
				catch(Exception e)
				{

				}
			}
		}
	}

	public static void main(String[] args)
	{
		GameRunner runner = new GameRunner();

		while(true)
		{
			System.out.print(runner.renderStats());
			runner.populationStep();

			if(fps > 0)
			{
				try
				{
					Thread.sleep(1000/fps);
				}
				catch(InterruptedException e)
				{ }
			}
		}
	}

	public void tick()
	{
		if(outputLog != null)
		{
			outputLog.append(renderStats());
		}
		populationStep();
	}

	public String renderStats()
	{
		String out = "";
		for (Map.Entry<String, Integer> entry : populationHistogram().entrySet())
		{
			if(true || entry.getValue()/(double) populationSize > 0)
			{
				out += Integer.toString(roundNumber) + "\t" + entry.getKey() + "\t" + entry.getValue() + "\n";
			}
		}

		return out;
	}
	
	public GameRunner()
	{
		this(null, 100, 1, 0.01, 0.01, 1, 10, 0.01, 0.01, 0.01, new GameAction[] {GameAction.COOPERATE}, new GameAction[]{GameAction.COOPERATE, GameAction.COOPERATE}, new GameAction[] {GameAction.STRATEGY}, 20);
	}

	public GameRunner(JTextArea textarea, int rpg, int ps, double n, double smr, int smmins, int smmaxs, double smsmut, double smmr, double omr, GameAction[] smchr, GameAction[] scrh, GameAction[] ochr, int rc)
	{
		// Ugly, the GameRunner was initially concieved to be the main class.
		outputLog = textarea;
		
		roundsPerGame = rpg;
		populationSize = ps;
		noise = n;
		strategyMutationRate = smr;
		startingMemoryMinSize = smmins;
		startingMemoryMaxSize = smmaxs;
		startingMemorySizeMutationRate = smsmut;
		startingMemoryMutationRate = smmr;
		overrideMutationRate = omr;

		replacementCount = rc;

		ac = new AgentComparator();
		population = new ArrayList<Agent>(populationSize);
		Agent a;

		Chromosome strategyChromosome = new Chromosome(new GameAction[]{GameAction.COOPERATE, GameAction.DEFECT});
		strategyChromosome.mutationRate = strategyMutationRate;
		strategyChromosome.chromosome = scrh;
			
		Chromosome overrideChromosome = new Chromosome(new GameAction[]{GameAction.COOPERATE, GameAction.DEFECT, GameAction.STRATEGY});
		overrideChromosome.maxLength = roundsPerGame;
		overrideChromosome.minLength = roundsPerGame;
		overrideChromosome.mutationRate = overrideMutationRate;
		overrideChromosome.chromosome = new GameAction[roundsPerGame];
		overrideChromosome.chromosome = ochr;

		Chromosome startingMemoryChromosome = new Chromosome(new GameAction[]{GameAction.COOPERATE, GameAction.COOPERATE});
		startingMemoryChromosome.mutationRate = startingMemoryMutationRate;
		//startingMemoryChromosome.chromosome = new GameAction[]{GameAction.COOPERATE};
		startingMemoryChromosome.chromosome = smchr;
			
		for(int i = 0; i < populationSize; i++)
		{
			a = new Agent(noise, startingMemorySizeMutationRate, startingMemoryMinSize, startingMemoryMaxSize, i);
			a.strategy = strategyChromosome.clone();
			a.override = overrideChromosome.clone();
			a.startingMemory = startingMemoryChromosome.clone();
			a.startingMemorySizeMutationRate = startingMemorySizeMutationRate;

			population.add(a);
		}

	}

	public void evaluatePopulation()
	{
		for(Agent a : population)
		{
			for(Agent b : population)
			{
				// Only play each agent pair once, ie. do not play A vs B AND B vs A.
				// Also prevent A vs A
				if(a.index < b.index)
				{
					playGame(a, b);
				}
			}
		}
	}

// populationStep() handled the transfering of agents (and their genomes) between generations
// First, all agents are evaluated with evaluatePopulation(), which runs all agents against each other and
// sets Agent.totalScore appropriately.
// Second, agents are sorted by their score, highest to lowest.
// Third, a new (empty) population called nextPopulation is created
// Fourth, the first (2 * <replacementCount>) agents are copied over to the new generation,
// as they are copied, they are also mutated.
// Fifth, remaining agents are copied and mutated to fill up nextPopulation to the appropriate number of agents.

// Note, when calling clone() on an agent, its totalScore is reset to zero.
// The "best" agents are transferred twice to the new generation, *however*
// 	One instance is cloned (by a call to Agent.clone()), the other is instead moved. We are thus using
// 	the *same* agent, why we need to reset the score manually
//
// Should you wish to keep some agents without mutation, simply comment out the line:
//	template.mutate(), which would keep the original agent without mutation
	
	public void populationStep()
	{
		evaluatePopulation();
		Collections.sort(population, ac);

		ArrayList<Agent> nextPopulation = new ArrayList<Agent>(populationSize);
		Agent template;

		for(int i = 0; i < replacementCount; i++)
		{
			template = population.get(i);
			nextPopulation.add(template.clone().mutate());
			template.totalScore = 0;
			template.mutate();
			nextPopulation.add(template);
		}
		
		for(int i = replacementCount; i < populationSize - replacementCount; i++)
		{
			nextPopulation.add(population.get(i).clone().mutate());
		}

		for(int i = 0; i < populationSize; i++)
		{
			nextPopulation.get(i).index = i;
		}
		
		population = nextPopulation;
		roundNumber++;
	}

	// Plays all roundsPerGame rounds between two players, increases their totalScore accordingly
	public void playGame(Agent player1, Agent player2)
	{
		GameAction p1move, p2move;
		GameAction[] history = new GameAction[roundsPerGame*2];

		player1.matchScore = 0;
		player2.matchScore = 0;

		for(int i = 0; i < roundsPerGame; i++)
		{
			p1move = player1.getNextMove(i, history, false);
			p2move = player2.getNextMove(i, history, true);

			history[2*i] = p1move;
			history[2*i+1] = p2move;
			

			if(p1move == GameAction.COOPERATE && p2move == GameAction.COOPERATE)
			{
				//System.out.println("Both cooperated");
				player1.giveScore(mutualCooperationScore);
				player2.giveScore(mutualCooperationScore);
			}
			else if(p1move == GameAction.COOPERATE && p2move == GameAction.DEFECT)
			{
				player1.giveScore(loseScore);
				player2.giveScore(winScore);
			}
			else if(p1move == GameAction.DEFECT && p2move == GameAction.COOPERATE)
			{
				player1.giveScore(winScore);
				player2.giveScore(loseScore);
			}
			else if(p1move == GameAction.DEFECT && p2move == GameAction.DEFECT)
			{
				player1.giveScore(mutualDefectionScore);
				player2.giveScore(mutualDefectionScore);
			}
		}
	}	
}


