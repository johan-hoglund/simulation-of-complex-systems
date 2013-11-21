import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GameRunner
{
	public static int roundsPerGame = 100;
	public static int populationSize = 20;
	public static double mutualCooperationScore = 3;
	public static double mutualDefectionScore = 1;
	public static double loseScore = 0;
	public static double winScore = 5;
	public static double noise = 0;
	public static int roundNumber = 0;
	public static int fps = 1;
	
	private ArrayList<Agent> population;

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

		for(int i = 0; i < 1000; i++)
		{
			runner.renderStats();
			runner.populationStep();

			try
			{
				Thread.sleep(1000/fps);
			}
			catch(InterruptedException e)
			{ }
		}
	}

	public void renderStats()
	{
		for (Map.Entry<String, Integer> entry : populationHistogram().entrySet()) {
				System.out.println(Integer.toString(roundNumber) + "\t" + entry.getKey() + "\t" + entry.getValue());
		}
	}

	public GameRunner()
	{
		population = new ArrayList<Agent>(populationSize);
		Agent a;

		Chromosome strategyChromosome = new Chromosome(new GameAction[]{GameAction.DEFECT, GameAction.COOPERATE});
		strategyChromosome.maxLength = 2;
		strategyChromosome.minLength = 2;
		strategyChromosome.mutationRate = 0.1;
		strategyChromosome.chromosome = new GameAction[]{GameAction.COOPERATE, GameAction.COOPERATE, GameAction.COOPERATE, GameAction.COOPERATE};

		Chromosome overrideChromosome = new Chromosome(new GameAction[]{GameAction.STRATEGY, GameAction.DEFECT, GameAction.COOPERATE});
		overrideChromosome.maxLength = roundsPerGame;
		overrideChromosome.minLength = roundsPerGame;
		overrideChromosome.mutationRate = 0;
		overrideChromosome.chromosome = new GameAction[roundsPerGame];
		for(int i = 0; i < roundsPerGame; i++)
		{
			overrideChromosome.chromosome[i] = GameAction.STRATEGY;
		}

		Chromosome startingMemoryChromosome = new Chromosome(new GameAction[]{GameAction.COOPERATE, GameAction.DEFECT});
		startingMemoryChromosome.maxLength = 2;
		startingMemoryChromosome.minLength = 2;
		startingMemoryChromosome.mutationRate = 0;
		startingMemoryChromosome.chromosome = new GameAction[]{GameAction.COOPERATE, GameAction.COOPERATE};


		for(int i = 0; i < populationSize; i++)
		{
			a = new Agent(noise);
			a.strategy = strategyChromosome.clone();
			a.override = overrideChromosome.clone();
			a.startingMemory = startingMemoryChromosome.clone();

			population.add(a);
		}
	}

	public void evaluatePopulation()
	{
		for(Agent a : population)
		{
			for(Agent b : population)
			{
				if(a != b)
				{
					for(int i = 0; i < roundsPerGame/2; i++)
					{
						playGame(a, b);
					}
				}
			}
		}
	}

	public void populationStep()
	{
		evaluatePopulation();
		Collections.sort(population, new AgentComparator());

		ArrayList<Agent> nextPopulation = new ArrayList<Agent>(populationSize);
		for(int i = 0; i < populationSize / 2; i++)
		{
			nextPopulation.add(population.get(i).clone().mutate());
			nextPopulation.add(population.get(i).clone().mutate());
		}
		
		population = nextPopulation;
		roundNumber++;
	}


	public void playGame(Agent player1, Agent player2)
	{
		GameAction p1move, p2move;
		GameAction[] history = new GameAction[roundsPerGame*2];

		for(int i = 0; i < roundsPerGame; i++)
		{
			p1move = player1.getNextMove(i, history);
			p2move = player2.getNextMove(i, history);

			history[2*i] = p1move;
			history[2*i+1] = p2move;
			
			if(p1move == GameAction.COOPERATE && p2move == GameAction.COOPERATE)
			{
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


