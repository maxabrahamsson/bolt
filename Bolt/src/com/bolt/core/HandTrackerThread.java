package com.bolt.core;
import org.OpenNI.*;

import com.bolt.hand.GestureDetected;
import com.bolt.hand.HandTrackLost;
import com.bolt.hand.HandTrackStart;
import com.bolt.hand.HandTrackUpdate;

import java.nio.ShortBuffer;
import java.util.HashMap;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.*;

import javax.swing.JFrame;

public class HandTrackerThread extends Component implements Runnable
{
	public HashMap <Integer,Point3D> hands=new HashMap<Integer,Point3D>();
	private static final long serialVersionUID = 1L;
	private OutArg<ScriptNode> scriptNode;
    private Context context;
    DepthGenerator depthGen;
    private GestureGenerator gestureGen;
    public HandsGenerator handsGen;    
    
    private byte[] imgbytes;
    private float histogram[];
    private BufferedImage bimg;

    int width, height;
    private final String SAMPLE_XML_FILE = "SamplesConfig.xml";
    
	private boolean shouldRun = true;
	private JFrame frame;
	
	public InputProcessor inputProcessor=null;
	Boolean showWindow=false;
	String TrackGesture;
	private void startTracking()
	{       
        try {
        	context = Context.createFromXmlFile(SAMPLE_XML_FILE, scriptNode);
        	
        	gestureGen = GestureGenerator.create(context);
			gestureGen.addGesture(TrackGesture);
	        gestureGen.getGestureRecognizedEvent().addObserver(new GestureDetected(this));	
	        
	        handsGen = HandsGenerator.create(context);
	        handsGen.getHandCreateEvent().addObserver(new HandTrackStart(this));
	        handsGen.getHandUpdateEvent().addObserver(new HandTrackUpdate(this));
	        handsGen.getHandDestroyEvent().addObserver(new HandTrackLost(this));
	        System.out.println("Gesture : "+TrackGesture);
		} catch (StatusException e) {
			e.printStackTrace();
		} catch (GeneralException e) {
			e.printStackTrace();
		}

	}
    public HandTrackerThread(InputProcessor acAdapter,String StartGesture)
    {
    	inputProcessor=acAdapter;
    	scriptNode = new OutArg<ScriptNode>(); 
        TrackGesture=StartGesture;
        startTracking();
        createWindow();      
    }
    public HandTrackerThread(InputProcessor acAdapter,String StartGesture,Boolean ShowWindow)
    {
    	inputProcessor=acAdapter;
    	scriptNode = new OutArg<ScriptNode>(); 
        TrackGesture=StartGesture;
        startTracking();
        if(ShowWindow){
        	createWindow();      
        }
        else
        {
        	showWindow=ShowWindow;
        }
    }
    private void createWindow()
    {
        try {
			depthGen = DepthGenerator.create(context);
			context.startGeneratingAll();
		} catch (GeneralException e1) {
			e1.printStackTrace();
		}
        DepthMetaData depthMD = depthGen.getMetaData();      
        histogram = new float[10000];
        width = depthMD.getFullXRes();
        height = depthMD.getFullYRes();
        
        imgbytes = new byte[width*height];
        
        DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width*height);
        Raster raster = Raster.createPackedRaster(dataBuffer, width, height, 8, null);
        bimg = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        bimg.setData(raster);
    	frame = new JFrame("OpenNI Hand Tracker");
    	
    	frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
    	frame.add("Center",this);
    	frame.pack();
    	frame.setVisible(true);   
    	showWindow=true;
    }
    private void closeWindow()
    {
    	frame.setVisible(false);
    	showWindow=false;
    }
    private void showWindow(Boolean show)
    {
    	if(showWindow!=show)
    	{
    		if(show)
    		{
    			createWindow();
    		}
    		else
    		{
    			closeWindow();
    		}
    	}
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
        	g.setColor(Color.RED);

        	for(Point3D position:hands.values())
        	{
            	try {
    				proj = depthGen.convertRealWorldToProjective(position);
    			} catch (StatusException e) {
    				e.printStackTrace();
    			}
            	g.drawArc((int)proj.getX()-10,(int)proj.getY()-10, 20, 20, 0, 360);
        	}
    }

	@Override
	public void run() {

	        while(shouldRun) {
	    		if(showWindow)
	    		{
	            updateDepth();
	            repaint();
	    		}
	        }
	        frame.dispose();
		
	}
}

