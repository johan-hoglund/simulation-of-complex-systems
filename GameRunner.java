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


	public void playGame(Agent player1, Agent player2)
	{
		//System.out.println("\nPlaying " + player1 + " to " + player2);
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
			

			//System.out.println("Player 1 score " + player1.totalScore + ", player 2 score " + player2.totalScore);
			if(p1move == GameAction.COOPERATE && p2move == GameAction.COOPERATE)
			{
				//System.out.println("Both cooperated");
				player1.giveScore(mutualCooperationScore);
				player2.giveScore(mutualCooperationScore);
			}
			else if(p1move == GameAction.COOPERATE && p2move == GameAction.DEFECT)
			{
			//	System.out.println("Player 1 cooperated, player 2 defected, player 1 gets score " + loseScore + ", player 2 gets score " + winScore);
				player1.giveScore(loseScore);
				player2.giveScore(winScore);
			}
			else if(p1move == GameAction.DEFECT && p2move == GameAction.COOPERATE)
			{
			//	System.out.println("Player 1 defected, player 2 cooperated, player 1 gets score " + winScore + ", player 2 gets score " + loseScore);
				player1.giveScore(winScore);
				player2.giveScore(loseScore);
			}
			else if(p1move == GameAction.DEFECT && p2move == GameAction.DEFECT)
			{
				//System.out.println("Both defected");
				player1.giveScore(mutualDefectionScore);
				player2.giveScore(mutualDefectionScore);
			}
		//	System.out.println("Player 1 score " + player1.totalScore + ", player 2 score " + player2.totalScore);
		}

		//System.out.println("After " + roundsPerGame + " iterations, " + player1.genomeString() + " has score " + player1.matchScore + ", " + player2.genomeString() + " has score " + player2.matchScore);
		//System.out.println("History: " + Arrays.toString(history));
	}	
}


