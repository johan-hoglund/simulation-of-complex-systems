import java.util.Arrays;

public class Agent implements Comparable<Agent>, Cloneable
{
	public Chromosome strategy;
	public Chromosome override;
	public Chromosome startingMemory;
	public double noise;
	public double totalScore = 0;
	public double matchScore = 0;
	public double startingMemorySizeMutationRate;
	public double startingMemoryMinSize;
	public double startingMemoryMaxSize;


	public Agent(double noiseLevel, double sizeMutationRate, double minSize, double maxSize)
	{
		noise = noiseLevel;
		startingMemorySizeMutationRate = sizeMutationRate;
		startingMemoryMinSize = minSize;
		startingMemoryMaxSize = maxSize;
	}

	public int computeHistoryId(GameAction[] history, int roundNumber, boolean firstPlayerPerspective)
	{
		int historySize = (int) Math.round(Math.log(strategy.size()) / Math.log(2));
		
		int id = 0;
		int pos;

		for(int i = 0; i < historySize; i++)
		{
			if(firstPlayerPerspective)
			{
				pos = (i%2 == 0) ? (roundNumber-1) * 2 - 2*i : (roundNumber-1) * 2 + 1 - 2*i;
	//			System.out.println("First player, round " + roundNumber + ", history: " + Arrays.toString(history) + ", pos: " + pos);
			}
			else
			{
				pos = (i%2 == 0) ? (roundNumber-1) * 2 - 2*i + 1 : (roundNumber-1) * 2 - 2*i;
	//			System.out.println("Second player, round " + roundNumber + ", history: " + Arrays.toString(history) + ", pos: " + pos);
			}
			if(pos >= 0)
			{
				if(history[pos] == GameAction.COOPERATE)
				{
					id += Math.pow(2, i);
				}
			}
			else
			{
				if(startingMemory.chromosome[i] == GameAction.COOPERATE)
				{
					id += Math.pow(2, i);
				}
			}
		}

		/*
		if(firstPlayerPerspective)
		{
			for(int i = 0; i < historySize; i++)
			{
				if((pos = roundNumber*2 - i) >= 0)
				{
					System.out.println("Looking in history at position " + pos);
					if(history[pos] == GameAction.COOPERATE)
					{
						id += Math.pow(2, i);
					}
				}
				else if(startingMemory.chromosome[i] == GameAction.COOPERATE)
				{
					System.out.println("Looking in starting memory at " + i);
					id += Math.pow(2, i);
				}
				else
				{
					System.out.println("Looking in starting memory at " + i);
				}
			}
			System.out.println("From first player perspective, history " + Arrays.toString(history) + " decodes to ID " + id);
		}
		else
		{
			for(int i = 0; i < historySize; i++)
			{
				if((pos = roundNumber*2 -1 - i) >= 0)
				{
					if(pos % 2 == 0)
					{
						if(history[pos+1] == GameAction.COOPERATE)
						{
							id += Math.pow(2, i);
						}
					}
					else
					{
						if(history[pos-1] == GameAction.COOPERATE)
						{
							id += Math.pow(2, i);
						}
					}
				}
				else if(startingMemory.chromosome[i] == GameAction.COOPERATE)
				{
					id += Math.pow(2, i);
				}
			}
			System.out.println("From second player perspective, history " + Arrays.toString(history) + " decodes to ID " + id);
		}
		*/


		return id;
	}

	public GameAction getNextMove(int roundNumber, GameAction[] history, boolean firstPlayerPerspective)
	{
		GameAction action;
		
		int historyId = computeHistoryId(history, roundNumber, firstPlayerPerspective);
		if(override.get(roundNumber) == GameAction.STRATEGY)
		{
			//System.out.println("Histody id: " + historyId + strategy.chromosome[historyId]);
			action = strategy.chromosome[historyId];
		}
		else if(override.get(roundNumber) == GameAction.DEFECT)
		{
			action = GameAction.DEFECT;
		}
		else
		{
		//	System.out.println("Override said cooperate");
			action = GameAction.COOPERATE;
		}

		if(Math.random() < noise)
		{
			action = (action == GameAction.DEFECT) ? GameAction.COOPERATE : GameAction.DEFECT;
		}

		//System.out.println("For history " + historyId + ", action: " + action + ", strategy: " + strategy);

		return action;
	}

	public String genomeString()
	{
		//return startingMemory.toString() + "\t" + strategy.toString() + "\t" + override.toString();
		return startingMemory.toString() + "\t" + strategy.toString();
	}

	public void giveScore(double score)
	{
		totalScore += score;
		matchScore += score;
		//System.out.println("Got score " + score + ", total score is now " + totalScore);
	}

	public int compareTo(Agent other)
	{
		return (int) (other.totalScore - totalScore);
	}

	public Agent clone()
	{
		Agent copy = new Agent(noise, startingMemorySizeMutationRate, startingMemoryMinSize, startingMemoryMaxSize);
		copy.strategy = strategy.clone();
		copy.override = override.clone();
		copy.startingMemory = startingMemory.clone();

		return copy;
	}

	public Agent mutate()
	{
		strategy.mutate();
		override.mutate();

		if(startingMemory.size() < startingMemoryMaxSize)
		{
			if(Math.random() < startingMemorySizeMutationRate)
			{
				strategy.grow(strategy.size());
				startingMemory.grow(1);
			}
		}
		if(startingMemory.size() > startingMemoryMinSize)
		{
			if(Math.random() < startingMemorySizeMutationRate)
			{
				strategy.shrink(strategy.size()/2);
				startingMemory.shrink(1);
			}
		}
		/*
		if(Math.random() < 0.01)
		{
			strategy.halve();
			startingMemory.halve();
		}
		*/

		return this;
	}
}
