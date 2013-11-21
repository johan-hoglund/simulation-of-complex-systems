public class Agent implements Comparable<Agent>, Cloneable
{
	public Chromosome strategy;
	public Chromosome override;
	public Chromosome startingMemory;
	public double noise;
	public double totalScore = 0;

	public int computeHistoryId(GameAction[] history, int roundNumber)
	{
		int historySize = (int) Math.round(Math.log(strategy.size()) / Math.log(2));

		if(roundNumber < historySize)
		{
			history = startingMemory.chromosome;
		}

		int id = 0;
		for(int i = 0; i < historySize; i++)
		{
			if(history[history.length-1-i] == GameAction.COOPERATE)
			{
				id += Math.pow(2, i);
			}
		}
	
		return id;
	}

	public GameAction getNextMove(int roundNumber, GameAction[] history)
	{
		GameAction action;
		
		int historyId = computeHistoryId(history, roundNumber);
		if(override.get(roundNumber) == GameAction.STRATEGY)
		{
			action = strategy.chromosome[historyId];
		}
		else if(override.get(roundNumber) == GameAction.DEFECT)
		{
			action = GameAction.DEFECT;
		}
		else
		{
			action = GameAction.COOPERATE;
		}

		if(Math.random() < noise)
		{
			action = (action == GameAction.DEFECT) ? GameAction.COOPERATE : GameAction.DEFECT;
		}

		return action;
	}

	public Agent(double noiseLevel)
	{
		noise = noiseLevel;
	}

	public String genomeString()
	{
		return startingMemory.toString() + "\t" + strategy.toString() + "\t" + override.toString();
	}

	public void giveScore(double score)
	{
		totalScore += score;
	}

	public int compareTo(Agent other)
	{
		return (int) (other.totalScore - totalScore);
	}

	public Agent clone()
	{
		Agent copy = new Agent(noise);
		copy.strategy = strategy.clone();
		copy.override = override.clone();
		copy.startingMemory = startingMemory.clone();

		return copy;
	}

	public Agent mutate()
	{
		strategy.mutate();
		override.mutate();

		if(Math.random() < 0.01)
		{
			strategy.grow();
			startingMemory.grow();
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
