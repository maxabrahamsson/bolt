import org.OpenNI.Point3D;

public class Starter extends ActionAdapter {
	public static void main(String[] args) {
		Starter test=new Starter();
		
		test.addActionListener(new ActionListener(){
			@Override
			public void ActionPerformed(ActionType type,Point3D position) {
				System.out.println(position.getX());
			}			
		});
	}

}
