public class Chromosome implements Cloneable
{
	GameAction[] geneSpace;
	int maxLength, minLength;
	public GameAction[] chromosome;
	double mutationRate = 0.01;

	public int size()
	{
		return chromosome.length;
	}

	public Chromosome(GameAction[] genes)
	{
		geneSpace = genes;
	}

	public GameAction get(int pos)
	{
		return chromosome[pos];
	}

	public Chromosome clone()
	{
		Chromosome copy = new Chromosome(geneSpace);
		copy.geneSpace = new GameAction[geneSpace.length];
		System.arraycopy(geneSpace, 0, copy.geneSpace, 0, geneSpace.length);
		
		copy.chromosome = new GameAction[chromosome.length];
		System.arraycopy(chromosome, 0, copy.chromosome, 0, chromosome.length);
		
		copy.maxLength = maxLength;
		copy.minLength = minLength;
		copy.mutationRate = mutationRate;

		return copy;
	}

	public void mutate()
	{
		for(int i = 0; i < chromosome.length; i++)
		{
			if(Math.random() < mutationRate)
			{
				chromosome[i] = geneSpace[(int) Math.round(Math.random() * (geneSpace.length-1))];
			}
		}
	}

	public String toString()
	{
		char[] out = new char[chromosome.length];
		for(int i = 0; i < chromosome.length; i++)
		{
			switch(chromosome[i])
			{
				case STRATEGY:
					out[i] = 'S';
					break;
				case COOPERATE:
					out[i] += 'C';
					break;
				case DEFECT:
					out[i] += 'D';
					break;
			}
		}
		return new String(out);
	}

	public void grow()
	{
			GameAction[] newChromosome = new GameAction[chromosome.length * 2];
			System.arraycopy(chromosome, 0, newChromosome, 0, chromosome.length);
			System.arraycopy(chromosome, 0, newChromosome, chromosome.length, chromosome.length);
			chromosome = newChromosome;
	}
}