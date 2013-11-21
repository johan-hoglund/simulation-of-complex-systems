import java.util.Comparator;

public class AgentComparator implements Comparator<Agent>
{
	public int compare(Agent a1, Agent a2)
	{
		return a1.compareTo(a2);
	}
}
