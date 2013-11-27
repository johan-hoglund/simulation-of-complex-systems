import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

public class GameRunner
{
	// Simulation parameters
	public static int roundsPerGame = 50;
	public static int populationSize = 100; // Must be even!
	public static int fps = 0;
	
	// Scores
	public static double mutualCooperationScore = 3;
	public static double mutualDefectionScore = 1;
	public static double loseScore = 0;
	public static double winScore = 5;

	// Game parameters
	public static double noise = 0.01;
	
	// Strategy parameters
	public static double strategyMutationRate = 0.01;
	
	// Starting memory parameters
	public static int startingMemoryMinSize = 1;
	public static int startingMemoryMaxSize = 5;
	public static double startingMemorySizeMutationRate = 0;
	public static double startingMemoryMutationRate = 0;

	// Override parameters
	public static double overrideMutationRate = 0.01;

	public AgentComparator ac;


	public static int roundNumber = 0;
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

	public static void main(String[] args)
	{
		GameRunner runner = new GameRunner();

		while(true)
		{
			runner.renderStats();
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

	public void renderStats()
	{
		for (Map.Entry<String, Integer> entry : populationHistogram().entrySet())
		{
			if(entry.getValue()/(double) populationSize > 0.01)
			{
				System.out.println(Integer.toString(roundNumber) + "\t" + entry.getKey() + "\t" + entry.getValue());
			}
		}
	}

	public GameRunner()
	{
		ac = new AgentComparator();
		population = new ArrayList<Agent>(populationSize);
		Agent a;

		Chromosome strategyChromosome = new Chromosome(new GameAction[]{GameAction.COOPERATE, GameAction.DEFECT});
		strategyChromosome.mutationRate = strategyMutationRate;
		strategyChromosome.chromosome = new GameAction[]{GameAction.COOPERATE, GameAction.COOPERATE, GameAction.DEFECT, GameAction.COOPERATE, GameAction.DEFECT, GameAction.DEFECT, GameAction.DEFECT, GameAction.COOPERATE, GameAction.DEFECT, GameAction.DEFECT, GameAction.DEFECT, GameAction.DEFECT, GameAction.DEFECT, GameAction.DEFECT, GameAction.DEFECT, GameAction.COOPERATE };

		//strategyChromosome.chromosome = new GameAction[]{GameAction.COOPERATE, GameAction.COOPERATE};
		
		Chromosome overrideChromosome = new Chromosome(new GameAction[]{GameAction.COOPERATE, GameAction.DEFECT, GameAction.STRATEGY});
		overrideChromosome.maxLength = roundsPerGame;
		overrideChromosome.minLength = roundsPerGame;
		overrideChromosome.mutationRate = overrideMutationRate;
		overrideChromosome.chromosome = new GameAction[roundsPerGame];
		for(int i = 0; i < roundsPerGame; i++)
		{
			overrideChromosome.chromosome[i] = GameAction.STRATEGY;
		}

		Chromosome startingMemoryChromosome = new Chromosome(new GameAction[]{GameAction.COOPERATE, GameAction.COOPERATE});
		startingMemoryChromosome.mutationRate = startingMemoryMutationRate;
		//startingMemoryChromosome.chromosome = new GameAction[]{GameAction.COOPERATE};
		startingMemoryChromosome.chromosome = new GameAction[]{GameAction.COOPERATE, GameAction.COOPERATE, GameAction.COOPERATE, GameAction.COOPERATE };

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

		
		for(int i = 0; i < populationSize; i++)
		{
	//		System.out.println("Rank " + i + ": " + population.get(i).genomeString());
		}
		
		ArrayList<Agent> nextPopulation = new ArrayList<Agent>(populationSize);
		Agent template;
		for(int i = 0; i < populationSize / 2; i++)
		{
			//System.out.println("Trasnfering to next population: " + population.get(i).genomeString());
			template = population.get(i);
			
			nextPopulation.add(template.clone().mutate());
			template.totalScore = 0;
			nextPopulation.add(template);
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


