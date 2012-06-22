import java.util.ArrayList;


public class ActionAdapter {
	ArrayList<ActionListener> listeners=new ArrayList<ActionListener>();
	Thread checkThread;
	HandTrackerThread handtrack=null;
	public ActionAdapter()
	{
		handtrack=new HandTrackerThread(this);
		checkThread=new Thread(handtrack);
		checkThread.start();
	}
	public void addActionListener(ActionListener actionListener)
	{
		listeners.add(actionListener);
	}
	public void triggerActions(Action action)
	{
		if(action.getType()==ActionType.HandUpdate)
		{
			for(int i=0; i<listeners.size(); i++)
			{
				listeners.get(i).ActionPerformed(action.getType(),action.getPosition());
			}		
		}
	}
}
