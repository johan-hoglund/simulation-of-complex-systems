// This is a more or less generic class to handle the starting memory, the override and the strategy
// The toString() methods handles rendering of the chromosome, i.e. it controls which characters are displayed
// for which action
//
// If you want to change the game logic, this is most probably *not* the place to do it :)
public class Chromosome implements Cloneable
{
	GameAction[] geneSpace;
	int maxLength, minLength;
	public GameAction[] chromosome;
	double mutationRate;

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
		char[] out = new char[chromosome.length*2];
		for(int i = 0; i < chromosome.length; i++)
		{
			switch(chromosome[i])
			{
				case STRATEGY:
					out[2*i] = 'S';
					break;
				case COOPERATE:
					out[2*i] += 'C';
					break;
				case DEFECT:
					out[2*i] += 'D';
					break;
			}
			out[2*i+1] += ' ';
		}
		return new String(out);
	}

	public void grow(int bits)
	{
			GameAction[] newChromosome = new GameAction[chromosome.length + bits];
			System.arraycopy(chromosome, 0, newChromosome, 0, chromosome.length);
			System.arraycopy(chromosome, 0, newChromosome, chromosome.length, bits);
			chromosome = newChromosome;
	}
	
	public void shrink(int bits)
	{
			GameAction[] newChromosome = new GameAction[chromosome.length - bits];
			System.arraycopy(chromosome, 0, newChromosome, 0, newChromosome.length);
			chromosome = newChromosome;
	}
}
