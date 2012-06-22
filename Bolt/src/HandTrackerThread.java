import org.OpenNI.*;

import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.*;

import javax.swing.JFrame;

class HandTrackerThread extends Component implements Runnable
{
	HashMap <Integer,Point3D> hands=new HashMap<Integer,Point3D>();
	class MyGestureRecognized implements IObserver<GestureRecognizedEventArgs>
	{

		@Override
		public void update(IObservable<GestureRecognizedEventArgs> observable,
				GestureRecognizedEventArgs args)
		{
			try
			{
				actionAdapter.triggerActions(new Action(ActionType.HandGesture));
				handsGen.StartTracking(args.getEndPosition());
				//gestureGen.removeGesture("Wave");
			} catch (StatusException e)
			{
				e.printStackTrace();
			}
		}
	}
	class MyHandCreateEvent implements IObserver<ActiveHandEventArgs>
	{
		public void update(IObservable<ActiveHandEventArgs> observable,ActiveHandEventArgs args){
			handPosition=args.getPosition();
			actionAdapter.triggerActions(new Action(ActionType.HandTrackStart,handPosition));
		}
	}
	class MyHandUpdateEvent implements IObserver<ActiveHandEventArgs>
	{
		public void update(IObservable<ActiveHandEventArgs> observable,ActiveHandEventArgs args){	
			handPosition=args.getPosition();
			actionAdapter.triggerActions(new Action(ActionType.HandUpdate,handPosition));
		}
	}
	class MyHandDestroyEvent implements IObserver<InactiveHandEventArgs>
	{
		public void update(IObservable<InactiveHandEventArgs> observable,InactiveHandEventArgs args){
			actionAdapter.triggerActions(new Action(ActionType.HandTrackLost));
			if (handPosition==null)
			{
				try
				{
					gestureGen.addGesture("Wave");
				} catch (StatusException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	private static final long serialVersionUID = 1L;
	private OutArg<ScriptNode> scriptNode;
    private Context context;
    private DepthGenerator depthGen;
    private GestureGenerator gestureGen;
    private HandsGenerator handsGen;    
    private Point3D handPosition;
    private byte[] imgbytes;
    private float histogram[];
    public Point3D lastPos=null;
    private BufferedImage bimg;
    int width, height;
    private final String SAMPLE_XML_FILE = "SamplesConfig.xml";
    
	private boolean shouldRun = true;
	private JFrame frame;
	
	ActionAdapter actionAdapter=null;
    public HandTrackerThread(ActionAdapter actionAdapter)
    {
    	this.actionAdapter=actionAdapter;
        try {
            scriptNode = new OutArg<ScriptNode>();
            context = Context.createFromXmlFile(SAMPLE_XML_FILE, scriptNode);
            gestureGen = GestureGenerator.create(context);
            gestureGen.addGesture("Wave");
            gestureGen.getGestureRecognizedEvent().addObserver(new MyGestureRecognized());
            
            handsGen = HandsGenerator.create(context);
            handsGen.getHandCreateEvent().addObserver(new MyHandCreateEvent());
            handsGen.getHandUpdateEvent().addObserver(new MyHandUpdateEvent());
            handsGen.getHandDestroyEvent().addObserver(new MyHandDestroyEvent());
            
            depthGen = DepthGenerator.create(context);
            DepthMetaData depthMD = depthGen.getMetaData();

			context.startGeneratingAll();
			
            
            histogram = new float[10000];
            width = depthMD.getFullXRes();
            height = depthMD.getFullYRes();
            
            imgbytes = new byte[width*height];
            
            DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width*height);
            Raster raster = Raster.createPackedRaster(dataBuffer, width, height, 8, null);
            bimg = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            bimg.setData(raster);

        } catch (GeneralException e) {
            e.printStackTrace();
            System.exit(1);
        }
    	frame = new JFrame("OpenNI Hand Tracker");
    	frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
    	frame.add("Center",this);
    	frame.pack();
    	frame.setVisible(true);
    }
    
    private void calcHist(ShortBuffer depth)
    {
        for (int i = 0; i < histogram.length; ++i)
        {
            histogram[i] = 0;
        }
        depth.rewind();
        int points = 0;
        while(depth.remaining() > 0)
        {
            short depthVal = depth.get();
            if (depthVal != 0)
            {
                histogram[depthVal]++;
                points++;
            }
        }        
        for (int i = 1; i < histogram.length; i++)
        {
            histogram[i] += histogram[i-1];
        }
        if (points > 0)
        {
            for (int i = 1; i < histogram.length; i++)
            {
                histogram[i] = (int)(256 * (1.0f - (histogram[i] / (float)points)));
            }
        }
    }

    void updateDepth()
    {
        try {
            DepthMetaData depthMD = depthGen.getMetaData();

            context.waitAnyUpdateAll();
            
            ShortBuffer depth = depthMD.getData().createShortBuffer();
            calcHist(depth);
            depth.rewind();          
            while(depth.remaining() > 0)
            {
                int pos = depth.position();
                short pixel = depth.get();
                imgbytes[pos] = (byte)histogram[pixel];
            }
        } catch (GeneralException e) {
            e.printStackTrace();
        }
    }


    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    public void paint(Graphics g) {
        DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width*height);
        Raster raster = Raster.createPackedRaster(dataBuffer, width, height, 8, null);
        bimg.setData(raster);
        g.drawImage(bimg, 0, 0, null);
        
        Point3D proj = null;
        if(handPosition!=null)
        {
        	g.setColor(Color.RED);
        	try {
				proj = depthGen.convertRealWorldToProjective(handPosition);
			} catch (StatusException e) {
				e.printStackTrace();
			}
        	g.drawArc((int)proj.getX(), (int)proj.getY(), 5, 5, 0, 360);
        }
    }

	@Override
	public void run() {
        while(shouldRun) {
            updateDepth();
            repaint();
        }
        frame.dispose();
		
	}
}

