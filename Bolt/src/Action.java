import org.OpenNI.Point3D;


public class Action {
	private ActionType actionType;
	private Point3D position;
	Action(ActionType actionType,Point3D position)
	{
		this.actionType=actionType;
		this.position=position;
	}
	Action(ActionType actionType)
	{
		this.actionType=actionType;
		this.position=null;
	}
	public Point3D getPosition()
	{
		return position;
	}
	public ActionType getType()
	{
		return actionType;
	}
}
