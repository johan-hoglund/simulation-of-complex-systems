import java.awt.*;
import javax.swing.*;
import java.awt.event.*;


public class GuiRunner extends JFrame
{
	GameRunner runner;
	Thread runnerThread;

	JButton startStopControl;
	JTextArea outputLog;
		
	JTextField roundsPerGameInput;
	JTextField populationSizeInput;
	JTextField noiseInput;
	JTextField strategyMutationRateInput;
	JTextField overrideMutationRateInput;
	JTextField strategyMemoryMinSizeInput;
	JTextField strategyMemoryMaxSizeInput;
	JTextField strategyMemorySizeMutationRateInput;
	
	JTextField overrideChromosomeInput;
	
	JTextField startingMemoryMutationRateInput;
	JTextField startingMemoryInput;
	JTextField strategyChromosomeInput;

	private class ResetListener implements ActionListener
	{
		GuiRunner guiRunner;
		public ResetListener(GuiRunner runner)
		{
			guiRunner = runner;
		}
		public void actionPerformed(ActionEvent e)
		{
			guiRunner.runner = null;
			guiRunner.runnerThread = null;
			guiRunner.outputLog.setText("");
			guiRunner.initializeRunner();
		}
	}

	private class StepListener implements ActionListener
	{
		GuiRunner guiRunner;
		public StepListener(GuiRunner runner)
		{
			guiRunner = runner;
		}
		public void actionPerformed(ActionEvent e)
		{
			initializeRunner();
			guiRunner.tick();
		}
	}

	private class StartStopListener implements ActionListener
	{
		GuiRunner guiRunner;
		public StartStopListener(GuiRunner runner)
		{
			guiRunner = runner;
		}
		public void actionPerformed(ActionEvent e)
		{
			initializeRunner();
			if(runnerThread.getState() == Thread.State.NEW)
			{
				runner.keepRunning = true;
				runnerThread.start();
				guiRunner.startStopControl.setText("Stop");
			}
			else
			{
				runner.keepRunning = !runner.keepRunning;
				guiRunner.startStopControl.setText(runner.keepRunning ? "Stop" : "Start");
			}
		}
	}

	private void initializeRunner()
	{
		if(runner == null)
		{
			try
			{
				int rpg = Integer.parseInt(roundsPerGameInput.getText());
				int ps = Integer.parseInt(populationSizeInput.getText());
				
				double n = Double.parseDouble(noiseInput.getText());
				double smr = Double.parseDouble(strategyMutationRateInput.getText());
				
				int smmins = Integer.parseInt(strategyMemoryMinSizeInput.getText());
				int smmaxs = Integer.parseInt(strategyMemoryMaxSizeInput.getText());
				
				double smsmut = Double.parseDouble(strategyMemorySizeMutationRateInput.getText());
				double smmr = Double.parseDouble(startingMemoryMutationRateInput.getText());
				
				double omr = Double.parseDouble(overrideMutationRateInput.getText());
				
				GameAction[] smchr = Agent.parseGameActionArray(startingMemoryInput.getText());
				GameAction[] schr = Agent.parseGameActionArray(strategyChromosomeInput.getText());
				GameAction[] ochr = Agent.parseGameActionArray(overrideChromosomeInput.getText());

				runner = new GameRunner(outputLog, rpg, ps, n, smr, smmins, smmaxs, smsmut, smmr, omr, smchr, schr, ochr);
			}
			catch(Exception e)
			{

			}
		}

		if(runnerThread == null)
		{
			runnerThread = new Thread(runner);
		}
	}

	public GuiRunner()
	{
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setVisible(true);
		setSize(500, 500);
		
		JPanel settings = new JPanel(new GridLayout(6, 5));
		startStopControl = new JButton("Start");
		JButton stepControl = new JButton("Step");
		
		JButton resetControl = new JButton("Reset");

		stepControl.addActionListener(new StepListener(this));
		startStopControl.addActionListener(new StartStopListener(this));
		resetControl.addActionListener(new ResetListener(this));

		JPanel outputPane = new JPanel();
		
		setLayout(new BorderLayout());

		
		outputLog = new JTextArea(20, 100);
		JScrollPane sp = new JScrollPane(outputLog);
		outputPane.add(sp);


		add(settings, BorderLayout.NORTH);
		add(outputPane, BorderLayout.SOUTH);

		settings.add(new JLabel("Rounds per game"));
		settings.add(new JLabel("Population size"));
		settings.add(new JLabel("Noise"));
		settings.add(new JLabel("Strategy mutation rate"));
		settings.add(new JLabel("Override mutation rate"));



		
		
		roundsPerGameInput = new JTextField("50");
		populationSizeInput = new JTextField("100");
		noiseInput = new JTextField("0.01");
		strategyMutationRateInput = new JTextField("0.01");
		overrideMutationRateInput = new JTextField("0.01");
		strategyMemoryMinSizeInput = new JTextField("1");
		strategyMemoryMaxSizeInput = new JTextField("10");
		strategyMemorySizeMutationRateInput = new JTextField("0.01");
		strategyChromosomeInput = new JTextField("DC");
		startingMemoryInput = new JTextField("C");
		startingMemoryMutationRateInput = new JTextField("0.01");
		overrideChromosomeInput = new JTextField("SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS");


		settings.add(roundsPerGameInput);
		settings.add(populationSizeInput);
		settings.add(noiseInput);
		settings.add(strategyMutationRateInput);
		settings.add(overrideMutationRateInput);
		
		settings.add(new JLabel("Strategy memory size mutation rate"));
		settings.add(new JLabel("Strategy memory min size"));
		settings.add(new JLabel("Strategy memory max size"));
		settings.add(new JLabel("Strategy chromosome"));
		settings.add(new JLabel("Starting memory"));
		
		
		settings.add(strategyMemorySizeMutationRateInput);
		settings.add(strategyMemoryMinSizeInput);
		settings.add(strategyMemoryMaxSizeInput);
		settings.add(strategyChromosomeInput);
		settings.add(startingMemoryInput);
		
		
		settings.add(new JLabel("Starting memory mutation rate"));
		settings.add(new JLabel("Override chromosome"));
		settings.add(new JLabel(""));
		settings.add(new JLabel(""));
		settings.add(new JLabel(""));
		
		settings.add(startingMemoryMutationRateInput);
		settings.add(overrideChromosomeInput);
		
		settings.add(stepControl);
		settings.add(resetControl);
		settings.add(startStopControl);
		
	}

	private void tick()
	{
		outputLog.append(runner.renderStats());
		runner.populationStep();
	}

	public static void main(String[] args)
	{
		GuiRunner gr = new GuiRunner();	

	}

}
