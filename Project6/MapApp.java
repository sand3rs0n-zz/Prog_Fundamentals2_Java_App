import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.border.*;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.EOFException;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.Map;
import javax.swing.JScrollPane;
import java.util.PriorityQueue;

/** Polygon stuff added, but works for shit. try making it identical to shape app
 * ; item listener to have duration not show. use boolean
   for query use lab 12 then add time. fix the Polygon so it associates with selected line
   currently associates with all*/
public class MapApp extends JFrame implements ActionListener
{
     /** Contains radio buttons for various RTS Items */
     JPanel selectionPanel;
     /** Map is drawn on this panel */
     JPanel drawingPanel;
     /** Contains the actionListPanel at the NORTH and the customPanel at the CENTER */
     JPanel actionPanel;
     /** Contains a JComboBox for actions and the UIelements for the parameters */
     JPanel actionListPanel;
     /** The contents of this panel changes based on the chosen RTS Item and the action*/
     JPanel customPanel; 
     /** This panel contains the save and load buttons */
     JPanel IOPanel;
     
     /** All created RTS Items are stored in this data structure as (name of the object, the object) pair */
     HashMap<String, Location> points = new HashMap();   
     HashMap<String, Line> lines = new HashMap();
     HashMap<String, Location> drawn = new HashMap();
     HashMap<String, BusStop> busstops = new HashMap();
     
     /** Each group of action parameter components is stored in a separate ArrayList of JComponents. 
      *  This data structure stores (rtsItem + actionMode, ArrayList of JComponent)  pairs  
      */
     HashMap<String, ArrayList<JComponent>> actionComponents = new HashMap();
     HashMap<String, Location> labels = new HashMap();
     
     /** Array of action types for location (busstop etc) */ 
     String[] locationActions = {"New", "Show", "Hide", "Move", "Write Line Info"};
     /** Array of action types for line */
     String[] lineActions = {"New", "Add BusStop", "Write Schedule"};
     /** Array of action types for point of interest */
     String[] poiActions = {"New", "Show", "Hide", "Move", "Search for Line"};
     /** Stores the RTS Item currently selected. Default is BusStop. */
     String rtsItem = "BusStop";
     /** Stores the selected action type. Default is New. */
     String actionMode = "New";
     
     /** This gets concatenated with rtsItem to store and fetch the relevant action list combo box */
     String actionListKeyword = "ActionList";
     
     /** Used to enter name of the map item */
     JTextField name;
     /** Used to enter x coordinate of the map item */
     JTextField x;
     /** Used to enter y coordinate of the map item */
     JTextField y;
     /** Used to enter the duration when adding next bus stop to a line */
     JTextField duration;
     /** Used to enter the hours and minutes of first and last service of a line */
     JTextField firstH, firstM, lastH, lastM;
     /** Used to enter the period by which the bus schedule changes */
     JTextField period;
     /** Used to enter the description of a point of interest */
     JTextField description;  
     /** Used to specify whether a busstop is also a time point */
     JCheckBox timePoint;

     /** Action list combo box for location type map items */
     JComboBox locationActionList;   
     /** Action list combo box for line */
     JComboBox lineActionList;
     /** Action list combo box for point of interest */
     JComboBox poiActionList;
     
     /** When a BusStop object gets created, its name is added to this list */
     JComboBox busStopList;
     /** When a BusStop object gets created, its name is added to this list */
     JComboBox busStopListDep;
     /** When a BusStop object gets created, its name is added to this list */
     JComboBox busStopListDest;
     /** When a Line object gets created, its name is added to this list */
     JComboBox lineList;
     /** When a PointOfInterest object gets created, its name is added to this list */
     JComboBox poiList;
     JComboBox poiListDep;
     JComboBox poiListDest;
     
     /** Holds the image of the map */
     BufferedImage mapImage;
     BufferedImage tpImage;
     BufferedImage bsImage;
     BufferedImage poiImage;

     /**Sets up the JFrame for Schedule*/
     JTextArea sched;
     JPanel panel = new JPanel();
     JScrollPane scrollBar = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                                    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
     JButton update = new JButton("Update");
     JFrame frame = new JFrame("Schedule");
     
     /**Sets up the JFrame for the Drawn Line*/
     JFrame lineFrame = new JFrame("Line Details");
     String[] colors = {"black", "blue", "cyan", "darkGray", "gray", "green", "lightGray", "magenta", "orange", "pink", "red", "white", "yellow"};   
     JComboBox colorList = new JComboBox(colors);
     String[] lineNames2 = {};
     JComboBox lineList2 = new JComboBox(lineNames2);
     JButton setLine = new JButton("Set Line");
     JPanel cPanel = new JPanel();
     JPanel lPanel = new JPanel();
     
     Collection<BusStop> locs;
     QueryResult result;
     JTextField poiH;
     JTextField poiM;
     
     /** Sets up the Line drawing feature */
     JRadioButton moveMode, drawMode;
     ArrayList<Integer> lineUpperX = new ArrayList<Integer>();
     ArrayList<Integer> lineUpperY = new ArrayList<Integer>();
     ArrayList<Integer> lineBottomX = new ArrayList<Integer>();
     ArrayList<Integer> lineBottomY = new ArrayList<Integer>();
     Polygon polygons = new Polygon();
     HashMap<String, Polygon> polyList = new HashMap();
     HashMap<Polygon, Color> polyColor = new HashMap();
     boolean drawingStarted = false;
     
     public void clearPolygon()
     {
         lineUpperX.clear();
         lineUpperY.clear();
         lineBottomX.clear();
         lineBottomY.clear();
         drawingStarted = false;
         drawingPanel.repaint();
     }
     
     String fileName = null;
     JFileChooser fileChooser = new JFileChooser();
     class loadButtonListener implements ActionListener  
     {
        public void actionPerformed(ActionEvent e)
        {
            int value = fileChooser.showOpenDialog(MapApp.this);
            if (value == JFileChooser.APPROVE_OPTION)
            {
                System.out.println("Reading Locations and Lines from " + fileChooser.getSelectedFile().getName());
                readInfo(fileChooser.getSelectedFile().getName());
                drawingPanel.repaint();
            }
        }
     }
     
     public void readInfo(String fileName) 
     {
        try { 
         this.fileName = fileName;
         if (!new File(fileName).exists())
         {   
            new File(fileName).createNewFile();             
            return;
         }  
         else {             
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(fileName));
            while (true)
               try {    
                    String name = input.readUTF();
                    Object obj = input.readObject();
                    if(obj instanceof Location)
                    {
                        Location location = (Location) obj;
                        points.put(name, location);
                        if(location instanceof BusStop)
                        {
                            busStopList.addItem(name);
                            busstops.put(name, (BusStop)location);
                        }
                        
                        if(location instanceof TimePoint)
                        {
                            location.setImage(tpImage);
                            busStopListDep.addItem(name);
                            busStopListDest.addItem(name);
                            busstops.put(name, (TimePoint)location);
                        }
                        else if(location instanceof BusStop)
                        {
                            location.setImage(bsImage);
                        }
                        else if(location instanceof PointOfInterest)
                        {
                            location.setImage(poiImage);
                            poiList.addItem(name);
                            poiListDep.addItem(name);
                            poiListDest.addItem(name);
                        }      
                    }
                    
                    else if(obj instanceof Line)
                    {
                        Line line = (Line) obj;
                        lines.put(name, line);
                        lineList.addItem(name);
                        lineList2.addItem(name);
                    }
               }     
               catch(EOFException e) 
               {
                    System.out.println("EOF reached!");
                    input.close();
                    break;
               }   
         }
        }           
        catch(IOException e) { e.printStackTrace(); System.out.println("Problem reading locations");}
        catch(ClassNotFoundException e) { System.out.println("Class couldn't be loaded when reading Location and subclass objects");}
     }
      
     class saveButtonListener implements ActionListener
     {
         public void actionPerformed(ActionEvent e)
         {
            try { 
             int value = fileChooser.showSaveDialog(MapApp.this);
             if (value == JFileChooser.APPROVE_OPTION)
             {
                 System.out.println("Writing locations and lines to " + fileChooser.getSelectedFile().getName());
                 writeInfo(fileChooser.getSelectedFile().getName());
             }
            }
            catch(IOException exc)
            {
                System.out.println("Error saving location");
                exc.printStackTrace();
            }
         }
     }
     
    /** Sets up the Search for Line feature */ 
    public static class QueryResult
    {
        Line line;
        Location departure;
        Location destination;
        int depHour;
        int depMin;
        int destHour;
        int destMin;
        
        public QueryResult(Line line, Location departure, Location destination)
        {
           this.line = line;
           this.departure = departure;
           this.destination = destination;
        }
        
        public QueryResult(Line line, Location departure, Location destination, int hour, int min)
        {
           this.line = line;
           this.departure = departure;
           this.destination = destination;
           this.destHour = hour;
           this.destMin = min;
        }

        public String toString()
        {
            Time dep = new Time(depHour, depMin);
            String depString = dep.toString();
            if(depHour == 0 && depMin == 0)
                return "The best line to take \nto go from " + departure +
                    "\nto " + destination + " is Line " + line.name;
            else
                return "The best line to take \nto go from " + departure + "\nat " 
                        + depString + "\nto " + destination + " is Line " + line.name;
        }
        
        public void setDepHour(int hour)
        {
            depHour = hour;
        }
        
        public void setDepMin(int min)
        {
            depMin = min;
        }
    }
    
    public static class Stop implements Comparable<Stop>
    {
        int distance;
        BusStop bs;
        Location loc;
        
        public Stop(Location loc, BusStop bs)
        {
            this.loc = loc;
            this.bs = bs;
            distance = loc.distanceFrom(bs);
        }
        
        public int compareTo(Stop s)
        {
            if(s.distance < this.distance)
                return 1;
            else if(s.distance == this.distance)
                return 0;
            else
                return -1;
        }
    }
    
    public static QueryResult search(Collection<BusStop> locations, Location departure, Location destination)
    {
        PriorityQueue<Stop> dests = new PriorityQueue<Stop>();
        PriorityQueue<Stop> deps = new PriorityQueue<Stop>();
        for(BusStop bs : locations)
        {
            Stop stop = new Stop(destination, bs);
            dests.offer(stop);
            stop = new Stop(departure, bs);
            deps.offer(stop);
        }
        
        ArrayList<Line> lines = new ArrayList();
        ArrayList<Stop> temp = new ArrayList(deps);
        BusStop deststop = dests.poll().bs;
        for(int i = 0; i < temp.size(); i++)
        {
            BusStop depstop = temp.get(i).bs; 
            lines = deststop.linesFromTo(depstop, deststop);
                
            if(lines.size() != 0)
            {
                BusStop dep = depstop;
                BusStop dest = deststop;
                Line line = lines.get(0);
                return new QueryResult(line, dep, dest);
            }
        }
        return null;
    }
    
    public static QueryResult search(Collection<BusStop> locations, Location departure, Location destination, int hour, int min)
    {
        PriorityQueue<Stop> dests = new PriorityQueue<Stop>();
        PriorityQueue<Stop> deps = new PriorityQueue<Stop>();
        for(BusStop bs : locations)
        {
            Stop stop = new Stop(destination, bs);
            dests.offer(stop);
            stop = new Stop(departure, bs);
            deps.offer(stop);
        }
        
        Time depTime = new Time(hour, min);
        ArrayList<Line> lines = new ArrayList();
        ArrayList<Stop> temp = new ArrayList(deps);
        BusStop deststop = dests.poll().bs;
        for(int i = 0; i < temp.size(); i++)
        {
            BusStop depstop = temp.get(i).bs; 
            lines = deststop.linesFromTo(depstop, deststop);
                
            if(lines.size() != 0)
            {
                BusStop dep = depstop;
                BusStop dest = deststop;
                Line line = lines.get(0);
                
                Time current = line.first;
                while(current.compareTo(depTime) != 1)
                { 
                    current = current.advanceMinutes(line.period);
                }   
                Time destTime = current.clone();
                Time nextTimePoint = destTime.clone();
                for(int j=0; j < line.route.size(); j++)
                {
                    nextTimePoint = nextTimePoint.advanceMinutes(line.timePoints.get(j));
                    if(line.route.get(j).name.equals(dest.name))
                        break;
                }
                destTime = nextTimePoint.clone();
                
                if(line.route.get(0).name.equals(dest.name))
                {
                    destTime = current.advanceMinutes(line.period);
                    Time nextTimePoint2 = current.clone();
                    for(int j=1; j < line.route.size(); j++)
                    {
                        nextTimePoint2 = nextTimePoint2.advanceMinutes(line.timePoints.get(j));
                        if(!line.route.get(j).name.equals(dest.name))
                            break;
                    }
                    current = nextTimePoint2.clone();
                }
                 
                QueryResult res = new QueryResult(line, dep, dest, destTime.getHours(), 
                                                      destTime.getMinutes());
                res.setDepMin(current.getMinutes());
                res.setDepHour(current.getHours());
                return res;
            }
        }
        return null;
    }

    public static ArrayList<BusStop> filterBusStops(ArrayList<Object> list)
    {
        ArrayList<BusStop> bsList = new ArrayList();
        for(Object o: list)
        if (o instanceof BusStop)
            bsList.add((BusStop)o);
        return bsList; 
    }
     
     public void writeInfo(String fileName) throws IOException
     {
         ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(fileName));
         Set<Map.Entry<String,Location>> allLocation = points.entrySet();
         for(Map.Entry<String,Location> m: allLocation)
         {   
             output.writeUTF(m.getKey());
             output.writeObject(m.getValue());
         } 
         
         Set<Map.Entry<String, Line>> allLine = lines.entrySet();
         for(Map.Entry<String, Line> l: allLine)
         {
             output.writeUTF(l.getKey());
             output.writeObject(l.getValue());
         }
         output.close();
     }
    
    class RtsDurationListener implements ItemListener
    {
        public void itemStateChanged(ItemEvent e)
        {
            if(e.getStateChange() == ItemEvent.SELECTED)
            {
                if(points.get(busStopList.getSelectedItem()) instanceof TimePoint)
                    duration.setEditable(true);
                else
                    duration.setEditable(false);
            }
        }
    }
     
     /** Inner class to handle events fired from RTS Item radio buttons */
    class RtsItemListener implements ActionListener
    {
         public void actionPerformed(ActionEvent e)
         {
             // If change in selection
             if (rtsItem.compareTo(e.getActionCommand()) != 0)
             {   
                actionMode = "New";
                if (rtsItem.equals("BusStop"))
                   locationActionList.setSelectedIndex(0);
                if (rtsItem.equals("Line"))
                   lineActionList.setSelectedIndex(0);  
                if (rtsItem.equals("PointOfInterest"))
                   poiActionList.setSelectedIndex(0);
             }
             // Stores the type of the object that is chosen
             rtsItem = e.getActionCommand();
             System.out.println(rtsItem);
             updateCustomComponent();
         }
    }
     
     /** Inner class to handle events fired from action list */
    class RtsItemActionListener implements ItemListener
    {
         public void itemStateChanged(ItemEvent e)
         {
            if(e.getStateChange() == ItemEvent.SELECTED) 
            {   
               actionMode = (String) e.getItem();
               System.out.println(actionMode);
               updateCustomComponent();
            }
         }
    }
    
    int lastX, lastY;
    ArrayList<Location> movingLocationObjs = new ArrayList();
     
    class MouseListener extends MouseAdapter
    {
        public void mousePressed(MouseEvent e)
        {
            lastX = e.getX();
            lastY = e.getY();
            if(moveMode.isSelected())
            {   
                System.out.println("Let's move starting at (" + lastX + "," + lastY + ")");
                Collection<Location> locationObjs = drawn.values();
                for(Location l: locationObjs)
                if (l.covers(lastX, lastY, 10, 10))
                    movingLocationObjs.add(l);
            }    
            else
            {   
                drawingStarted = true;    
                System.out.println("Drawing starts at (" + lastX + "," + lastY + ")");
                lineUpperX.add(lastX); 
                lineUpperY.add(lastY - 5);
                lineBottomX.add(lastX);
                lineBottomY.add(lastY + 5);
            } 
        }
        
        public void mouseClicked(MouseEvent e) //set up frame for color
        {
            if (drawingStarted && e.getButton() == MouseEvent.BUTTON3)
            {
                 polygons = generatePolygon();
                 clearPolygon();
                 cPanel.removeAll();
                 lPanel.removeAll();
                 /** For the Drawn Line info */
                 lPanel.add(new JLabel("Line"));
                 lPanel.add(lineList2);
                 cPanel.add(new JLabel("Color"));
                 cPanel.add(colorList);
                 lineFrame.add(lPanel, BorderLayout.NORTH);
                 lineFrame.add(cPanel, BorderLayout.CENTER);
                 lineFrame.add(setLine, BorderLayout.SOUTH);
                 
                 lineFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                 lineFrame.setSize(220,150); 
                 lineFrame.setVisible(true);
            }
        }
        
        public void mouseReleased(MouseEvent e)
        {
            if(moveMode.isSelected())
            {
                lastX = e.getX();
                lastY = e.getY();
                try
                {
                    Collection<Location> locationObjs = drawn.values();
                    for(Location l: locationObjs)
                    if (l.covers(lastX, lastY, 10, 10) && l instanceof PointOfInterest)
                    {
                        poiList.setSelectedItem((String)l.name);
                        try
                        {
                            System.out.println(((PointOfInterest)points.get(poiList.getSelectedItem())).getDescription());
                            JOptionPane.showMessageDialog(poiList, ((PointOfInterest)points.get(poiList.getSelectedItem())).getDescription());
                        }
                        catch(Exception exc)
                        {
                        }
                    }
                    System.out.println("Move ended at (" + e.getX() + "," + e.getY() + ")");
                    busStopList.setSelectedItem((String)((movingLocationObjs.get(0)).name));
                    movingLocationObjs.clear();
                }
                catch(Exception exc)
                {
                }
            }
            else if(e.getButton() == MouseEvent.BUTTON1)
            {
                 clearPolygon();
            }
        }
    }
    
    class MouseMotionListener extends MouseMotionAdapter
    {
        public void mouseDragged(MouseEvent e)
        {
            int x = e.getX();
            int y = e.getY();
            
            if(moveMode.isSelected())
            {
                for(int i=0; i < movingLocationObjs.size(); i++)
                {
                    Location l = movingLocationObjs.get(i);
                    l.setX(l.getX() + x - lastX);
                    l.setY(l.getY() + y - lastY);
                }
            }
            else if (drawingStarted) 
            {
                 if (Math.abs(lastX - x) == 0)
                 {
                    lineUpperX.add(x + 5);
                    lineUpperY.add(y);
                    lineBottomX.add(x - 5);
                    lineBottomY.add(y); 
                    drawingPanel.repaint();
                 }
                 else 
                 {
                    lineUpperX.add(x);
                    lineUpperY.add(y - 5);
                    lineBottomX.add(x);
                    lineBottomY.add(y + 5);
                    drawingPanel.repaint();
                 }  
            }
            
            lastX = x;
            lastY = y;
            drawingPanel.repaint();
        }
        
        public void mouseMoved(MouseEvent e)
        { 
            lastX = e.getX();
            lastY = e.getY();
            if(moveMode.isSelected())
            {
                Set<Map.Entry<String, Location>> locationTupleSet = drawn.entrySet();
                int change = 0;
                for(Map.Entry m: locationTupleSet)
                     if (((Location)m.getValue()).covers(lastX, lastY, 10, 10))
                     {  
                         change++;  
                         labels.put((String)m.getKey(), (Location)m.getValue());
                     }  
                     else 
                     {       
                        if (labels.remove(m.getKey()) != null)
                            change++; 
                     }       
                if (change > 0)
                    drawingPanel.repaint(); 
            }
        }
    }
    
    class RequestFocusListener extends MouseAdapter
    {
         public void mousePressed(MouseEvent e)
         {
            requestFocusInWindow();
         }
    }
    
    Polygon generatePolygon()
    {
       if (lineUpperX.size() > 0)
       {       
         int[] xP = new int[2 * lineUpperX.size() + 1];
         int[] yP = new int[2 * lineUpperY.size() + 1];
         int i;
         for(i=0; i < lineUpperX.size(); i++)
         {
            xP[i] = lineUpperX.get(i);
            yP[i] = lineUpperY.get(i);              
         }
         for(int j=i=lineBottomX.size() - 1; i >= 0; i--)
         {
            xP[j + lineBottomX.size() - i] = lineBottomX.get(i);
            yP[j + lineBottomX.size() - i] = lineBottomY.get(i); 
         }
         xP[2 * lineUpperX.size()] = xP[0];
         yP[2 * lineUpperX.size()] = yP[0];
 
         return new Polygon(xP, yP, xP.length);
       }
       else 
        return null;
    }
    
    /** Creates a UI that consistes of 3 panels.
     * The left panel provides choices for RTS Items.
     * The middle panel provides choices for action types and the relevant data fields.
     * The right panel draws the map items based on the chosen map item and action type.
     */ 
    public MapApp(String s)  
    {
      super(s);
      
      // Create the left panel that holds radio buttons for shape types
      selectionPanel = new JPanel(new GridLayout(3, 1, 5, 5));
      selectionPanel.setBorder(new TitledBorder("Items"));
      // By default rectangle button will be selected, determined by the boolean parameter to the constructor
      JRadioButton busStopButton = new JRadioButton("BusStop", true);
      JRadioButton lineButton = new JRadioButton("Line", false);
      JRadioButton poiButton = new JRadioButton("PointOfInterest", false);
      
      busStopButton.setMnemonic('B');
      lineButton.setMnemonic('L');
      poiButton.setMnemonic('P');
      
      // ButtonGroup class helps us ensure only one shape can be selected at a time
      ButtonGroup group = new ButtonGroup();
      group.add(busStopButton);
      group.add(lineButton);
      group.add(poiButton);
      
      RtsItemListener rtsItemListener = new RtsItemListener();
      /** rtsItemListener object receives events from busStopButton */
      busStopButton.addActionListener(rtsItemListener);
      /** rtsItemListener object receives events from lineButton */
      lineButton.addActionListener(rtsItemListener);
      /** rtsItemListener object receives events from poiButton */
      poiButton.addActionListener(rtsItemListener);
      selectionPanel.add(busStopButton); 
      selectionPanel.add(lineButton);
      selectionPanel.add(poiButton);
     
      try {
        mapImage  = ImageIO.read(new File("ufMap.jpg"));
        tpImage = ImageIO.read(new File("black.jpg"));
        bsImage = ImageIO.read(new File("rectangle.jpg"));
        poiImage = ImageIO.read(new File("star.jpg"));
      }
      catch (IOException e) { System.out.println("Could not load an image");}

      // A panel for drawing the shapes that are created and for redrawing any changes related to them
      drawingPanel = new JPanel() {
         public void paintComponent(Graphics g)
         {
             super.paintComponent(g);
             g.clearRect(0,  0,  getWidth(),  getHeight());
             g.drawImage(mapImage, 0, 0, getWidth(), getHeight(), null);
             
             Collection<Location> items = drawn.values();
             for(Location r : items)
                r.draw(g, 10, 10);
                
             Color bfd = g.getColor();
             for(Polygon p : polyList.values())
             {
                 g.setColor(polyColor.get(p));
                 g.fillPolygon(p);
             }
             Polygon currentPolygon = generatePolygon();
             if (currentPolygon != null)
                g.fillPolygon(currentPolygon);
             g.setColor(bfd);
               
             if(moveMode.isSelected())
             {
                 FontMetrics fontMetrics = g.getFontMetrics();
                 Color prev = g.getColor();
                 Set<Map.Entry<String, Location>> labelSet = labels.entrySet();
                 for(Map.Entry m: labelSet)
                 {   
                     int height = fontMetrics.getHeight();
                     int width = fontMetrics.stringWidth((String)m.getKey());
                     int x = ((Location)m.getValue()).getX();
                     int y = ((Location)m.getValue()).getY();
                     if (y > getHeight())
                        y =  getHeight();
                     else if (y - height < 0)
                        y = height;
                     if (x + width > getWidth())
                        x = getWidth() - width;
                     else if (x < 0)
                        x = 0;
                     g.setColor(new Color(255, 215, 0));
                     g.fillRect(x, y - height, width, height);
                     g.setColor(Color.black);
                     g.drawString((String)m.getKey(), x, y);
                 }
                 g.setColor(prev);
             }
         }

         public Dimension getPreferredSize()
         {
           return new Dimension(300, 290);
         }  
      };
      drawingPanel.setBorder(new TitledBorder("Map"));
      drawingPanel.add(new JLabel("Map"));
      drawingPanel.setPreferredSize(new Dimension(50, 50)); 
      drawingPanel.addMouseListener(new MouseListener());
      drawingPanel.addMouseMotionListener(new MouseMotionListener());
      
      //Move and Draw features
      moveMode = new JRadioButton("Move & Show Mode");
      drawMode = new JRadioButton("Draw Mode");
      ButtonGroup drawingModeGroup = new ButtonGroup();
      drawingModeGroup.add(moveMode);
      drawingModeGroup.add(drawMode);
      moveMode.setSelected(true);
      JPanel buttonPanel = new JPanel();
      buttonPanel.add(moveMode);
      buttonPanel.add(drawMode);
      JPanel outerDrawPanel = new JPanel();
      outerDrawPanel.setLayout(new BorderLayout());
      outerDrawPanel.add(drawingPanel, BorderLayout.NORTH);
      outerDrawPanel.add(buttonPanel, BorderLayout.SOUTH);
      
      // This panel lets users save and load their data
      IOPanel = new JPanel();
      JButton loadButton = new JButton("Load");
      loadButton.addActionListener(new loadButtonListener());
      JButton saveButton = new JButton("Save");
      saveButton.addActionListener(new saveButtonListener());
      IOPanel.add(saveButton);
      IOPanel.add(loadButton);
      
      // This middle panel lets users choose an action on the RTS Items
      actionPanel = new JPanel(new BorderLayout(5,5));
      actionPanel.setBorder(new TitledBorder("Actions"));
      actionListPanel = new JPanel();
      customPanel = new JPanel();
      actionPanel.add(actionListPanel, BorderLayout.NORTH);
      actionPanel.add(customPanel, BorderLayout.CENTER);
      /** Initializes the customPanel that holds the fields for parameters */
      makeCustomPanelComponents();      
          
      //Sets Layout
      setLayout(new BorderLayout(5,5));
      add(outerDrawPanel, BorderLayout.EAST);
      add(selectionPanel, BorderLayout.WEST); 
      add(actionPanel, BorderLayout.CENTER);
      add(IOPanel, BorderLayout.NORTH);
      
      //Sets focus
      setFocusable(true);
      addMouseListener(new RequestFocusListener());
      
      //Sets up part of Schedule frame
      frame.add(scrollBar);
      frame.add(update, BorderLayout.SOUTH);

    }
    

    public void makeCustomPanelComponents()
    {   
        //Adds action listener to setLine JButton
        setLine.addActionListener((ActionListener)this);
        
        /** For displaying the actions for Location type objects */
        locationActionList = new JComboBox(locationActions); 
        /** an RtsItemActionListener object receives events from locationActionList */
        locationActionList.addItemListener(new RtsItemActionListener());
        JPanel locActionPanel = new JPanel();
        locActionPanel.add(locationActionList);
        ArrayList<JComponent> bsActionList = new ArrayList();
        bsActionList.add(locActionPanel);        
        actionComponents.put("BusStop" + actionListKeyword, bsActionList);
     
        /** For diplaying the actions for Line objects */ 
        lineActionList = new JComboBox(lineActions);
        /** an RtsItemActionListener object receives events from lineActionList */
        lineActionList.addItemListener(new RtsItemActionListener());
        JPanel lineActionPanel = new JPanel();
        lineActionPanel.add(lineActionList);
        ArrayList<JComponent> lnActionList = new ArrayList();
        lnActionList.add(lineActionPanel);
        actionComponents.put("Line" + actionListKeyword, lnActionList);
        
        /** For diplaying the actions for Point of Interest objects */ 
        poiActionList = new JComboBox(poiActions);
        /** an RtsItemActionListener object receives events from poiActionList */
        poiActionList.addItemListener(new RtsItemActionListener());
        JPanel poiActionPanel = new JPanel();
        poiActionPanel.add(poiActionList);
        ArrayList<JComponent> poiActionList = new ArrayList();
        poiActionList.add(poiActionPanel);
        actionComponents.put("PointOfInterest" + actionListKeyword, poiActionList);
        
        /** For entering the name of the map item */
        JLabel nLabel = new JLabel("Name");
        name = new JTextField(10);
        JPanel nPanel = new JPanel();
        nPanel.add(nLabel);
        nPanel.add(name);

        /**For entering Duration*/
        JLabel dLabel = new JLabel("Duration");
        duration = new JTextField("Duration", 3);
        JPanel dPanel = new JPanel();
        dPanel.add(dLabel);
        dPanel.add(duration);
        
        /** For entering the x and y coordinates */
        JLabel xLabel = new JLabel("x");
        x = new JTextField(4);
        JPanel xPanel = new JPanel();
        xPanel.add(xLabel);
        xPanel.add(x);
        JLabel yLabel = new JLabel("y");
        y = new JTextField(4);
        JPanel yPanel = new JPanel();
        yPanel.add(yLabel);
        yPanel.add(y);

        firstH = new JTextField(4);
        firstM = new JTextField(4);
        JPanel firstPanel = new JPanel();
        firstPanel.add(new JLabel("First"));
        firstPanel.add(firstH);
        firstPanel.add(new JLabel(":"));
        firstPanel.add(firstM);

        lastH = new JTextField(4);
        lastM = new JTextField(4);
        JPanel lastPanel = new JPanel();
        lastPanel.add(new JLabel("Last"));
        lastPanel.add(lastH);
        lastPanel.add(new JLabel(":"));
        lastPanel.add(lastM);

        period = new JTextField(4);
        JPanel periodPanel = new JPanel();
        periodPanel.add(new JLabel("Period"));
        periodPanel.add(period);
        
        /** For checking or unchecking timePoint feature */
        JLabel timePointLabel = new JLabel("Time Point?");
        timePoint = new JCheckBox("", false);
        timePoint.addActionListener((ActionListener)this);
        JPanel tPanel = new JPanel();
        tPanel.add(timePointLabel);
        tPanel.add(timePoint);
        
        /** For doing the chosen action on the selected object based on the entered parameter values */
        JButton ok = new JButton("OK");
        ok.setBackground(Color.blue);
        /** See actionPerformed method to see the action taken */
        ok.addActionListener((ActionListener)this);
        
        /** As objects get created their names will be added to the corresponding list */
        String[] busStopNames = {};
        busStopList = new JComboBox(busStopNames);
        busStopListDep = new JComboBox(busStopNames);
        busStopListDest = new JComboBox(busStopNames);
        String[] lineNames = {};
        lineList = new JComboBox(lineNames);
        JPanel bsListPanel = new JPanel();
        bsListPanel.add(new JLabel("BusStop"));
        bsListPanel.add(busStopList);
        JPanel bsListPanelDep = new JPanel();
        bsListPanelDep.add(new JLabel("Departure"));
        bsListPanelDep.add(busStopListDep);
        JPanel bsListPanelDest = new JPanel();
        bsListPanelDest.add(new JLabel("Destination"));
        bsListPanelDest.add(busStopListDest);

        JPanel lnListPanel = new JPanel();
        lnListPanel.add(new JLabel("Line"));
        lnListPanel.add(lineList);
        
        String[] poiNames = {};
        poiList = new JComboBox(poiNames);
        poiListDep = new JComboBox(poiNames);
        poiListDest = new JComboBox(poiNames);
        JPanel poiListPanel = new JPanel();
        poiListPanel.add(new JLabel("PointOfInterest"));
        poiListPanel.add(poiList);
        JPanel poiTime = new JPanel();
        poiH = new JTextField(4);
        poiM = new JTextField(4);
        poiTime.add(new JLabel("Time"));
        poiTime.add(poiH);
        poiTime.add(new JLabel(":"));
        poiTime.add(poiM);
        
        /** For entering a description */
        JLabel descriptionLabel = new JLabel("Description");
        description = new JTextField(10);
        JPanel descriptionPanel = new JPanel();
        descriptionPanel.add(descriptionLabel);
        descriptionPanel.add(description);
        
        /** For selecting POI departures and destinations */
        JPanel poiDepartures = new JPanel();
        poiDepartures.add(new JLabel("Departure"));
        poiDepartures.add(poiListDep);
        JPanel poiDestinations = new JPanel();
        poiDestinations.add(new JLabel("Destination"));
        poiDestinations.add(poiListDest);
        JButton search = new JButton("Search");
        search.addActionListener((ActionListener)this);
        
        /** Custom panel components for creating a new busstop */
        ArrayList<JComponent> newBSComponents = new ArrayList();
        newBSComponents.add(nPanel);
        newBSComponents.add(xPanel);
        newBSComponents.add(yPanel);
        newBSComponents.add(tPanel);      
        newBSComponents.add(ok);
        actionComponents.put("BusStopNew", newBSComponents);
        
        /** Custom panel components for moving a busstop */
        ArrayList<JComponent> moveBSComponents = new ArrayList();
        moveBSComponents.add(bsListPanel);
        moveBSComponents.add(xPanel);
        moveBSComponents.add(yPanel);
        moveBSComponents.add(ok);
        actionComponents.put("BusStopMove", moveBSComponents);     

        /** Custom panel components for showing a busstop on the map */
        ArrayList<JComponent> showBSComponents = new ArrayList();
        showBSComponents.add(bsListPanel);
        showBSComponents.add(ok);
        actionComponents.put("BusStopShow", showBSComponents);
        
        /** Custom panel components for hiding a bus stop from the map display */
        ArrayList<JComponent> hideBSComponents = new ArrayList();
        hideBSComponents.add(bsListPanel);
        hideBSComponents.add(ok);
        actionComponents.put("BusStopHide", hideBSComponents);
        
        /** Custom panel components for showing the lines that stop at a busstop */
        ArrayList<JComponent> showLnsComponents = new ArrayList();
        showLnsComponents.add(bsListPanel);
        showLnsComponents.add(ok);
        actionComponents.put("BusStopWrite Line Info", showLnsComponents);       
        
        /** Custom panel components for creating a bus line */
        ArrayList<JComponent> newLNComponents = new ArrayList();
        newLNComponents.add(nPanel);
        newLNComponents.add(bsListPanelDep);
        newLNComponents.add(bsListPanelDest);
        newLNComponents.add(firstPanel);
        newLNComponents.add(lastPanel);
        newLNComponents.add(periodPanel);
        newLNComponents.add(ok);
        actionComponents.put("LineNew", newLNComponents);
        
        /** Custom panel components for adding a bus stop to a line */
        ArrayList<JComponent> addBSComponents = new ArrayList();
        addBSComponents.add(bsListPanel);
        addBSComponents.add(lnListPanel);
        addBSComponents.add(dPanel);
        addBSComponents.add(ok);
        actionComponents.put("LineAdd BusStop", addBSComponents); 
        
        /** Custom panel components for writing the schedule of a line to a file */
        ArrayList<JComponent> writeSchComponents = new ArrayList();
        writeSchComponents.add(lnListPanel);
        writeSchComponents.add(ok);
        actionComponents.put("LineWrite Schedule", writeSchComponents);  
        
        /** Custom Panel components for creating a new point of interest */
        ArrayList<JComponent> newPOIComponents = new ArrayList();
        newPOIComponents.add(nPanel);
        newPOIComponents.add(xPanel);
        newPOIComponents.add(yPanel);
        newPOIComponents.add(descriptionPanel);
        newPOIComponents.add(ok);
        actionComponents.put("PointOfInterestNew", newPOIComponents);
        
        /** Custom panel components for moving a point of interest */
        ArrayList<JComponent> movePOIComponents = new ArrayList();
        movePOIComponents.add(poiListPanel);
        movePOIComponents.add(xPanel);
        movePOIComponents.add(yPanel);
        movePOIComponents.add(ok);
        actionComponents.put("PointOfInterestMove", movePOIComponents);     

        /** Custom panel components for showing a point of interest on the map */
        ArrayList<JComponent> showPOIComponents = new ArrayList();
        showPOIComponents.add(poiListPanel);
        showPOIComponents.add(ok);
        actionComponents.put("PointOfInterestShow", showPOIComponents);
        
        /** Custom panel components for hiding a point of interest from the map display */
        ArrayList<JComponent> hidePOIComponents = new ArrayList();
        hidePOIComponents.add(poiListPanel);
        hidePOIComponents.add(ok);
        actionComponents.put("PointOfInterestHide", hidePOIComponents);
   
        /** Custom panel components for query*/
        ArrayList<JComponent> queryComponents = new ArrayList();
        queryComponents.add(poiDepartures);
        queryComponents.add(poiDestinations);
        queryComponents.add(poiTime);
        queryComponents.add(search);
        actionComponents.put("PointOfInterestSearch for Line", queryComponents);
    }
    
    private void resetFields()
    {
        name.setText("");
        x.setText("");
        y.setText("");
        duration.setText("");
        period.setText("");
        firstH.setText("");
        firstM.setText("");
        lastH.setText("");
        lastM.setText("");
        description.setText("");
        timePoint.setSelected(false);
    }
    
    /** Removes the UI components in customPanel and adds the new components based on the selected shape and the action type */
    private void updateCustomComponent()
    {
       resetFields();   
       actionListPanel.removeAll();
       actionListPanel.add(actionComponents.get(rtsItem + actionListKeyword).get(0), BorderLayout.NORTH);
       actionListPanel.revalidate();
       actionListPanel.repaint();
       
        busStopList.addItemListener(new RtsDurationListener());
       customPanel.removeAll();
       ArrayList<JComponent> list = actionComponents.get(rtsItem + actionMode);
       
       for(int i=0; i < list.size(); i++)
          customPanel.add(list.get(i));
       // Updates the view of the customPanel with new components
       customPanel.revalidate();
       customPanel.repaint();     
    }
    
    /** Handles the OK button events */
    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().compareTo("OK") == 0)
        {
            System.out.println((rtsItem + actionMode));
            doAction();
        }
        
        if(e.getActionCommand().compareTo("Set Line") == 0)
        {
            System.out.println("Line set");
            polyList.put((String)lineList2.getSelectedItem(), polygons);
            polyColor.put(polygons, ColorDecoder.getColor((String)colorList.getSelectedItem()));
            drawingPanel.repaint();
            lineFrame.dispose();
            polygons = new Polygon();
        }
        
        if(e.getActionCommand().compareTo("Search") == 0)
        {
            queryAction();
        }
        
        if(e.getActionCommand().compareTo("Update") == 0)
        {
            System.out.println("Update Schedule");
            frame.dispose();
            panel.removeAll();
            System.out.println(lines.get(lineList.getSelectedItem()).getSchedule());
            sched = new JTextArea(lines.get(lineList.getSelectedItem()).getSchedule());
            panel.add(sched);
            update.addActionListener((ActionListener)(this));
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(250,300); 
            frame.setVisible(true);
        }
    }
    
    private void queryAction()
    {
        locs = busstops.values();
        if(poiH.getText().equals(""))
            result = search(locs, points.get(poiListDep.getSelectedItem()),
                points.get(poiListDest.getSelectedItem()));
        else
            result = search(locs, points.get(poiListDep.getSelectedItem()),
                points.get(poiListDest.getSelectedItem()), Integer.parseInt(poiH.getText()),
                Integer.parseInt(poiM.getText()));
        if(result == null)
            JOptionPane.showMessageDialog(poiList, "No applicable line found!");
        System.out.println(result);
        
        for(BusStop bs : locs)
        {
            if(!(bs.lines.contains(result.line)))
                locs.remove(bs);
        }
        
        JFrame queryFrame = new JFrame("Search Results");
        JPanel queryDescription = new JPanel();
        JTextArea qLabel = new JTextArea (result.toString());
        queryDescription.add(qLabel);
        
        JPanel queryMap = new JPanel()
        {
            public void paintComponent(Graphics g)
            {
                super.paintComponent(g);
                g.clearRect(0,  0,  getWidth(),  getHeight());
                g.drawImage(mapImage, 0, 0, getWidth(), getHeight(), null);
                
                Collection<BusStop> items = locs;
                for(BusStop r : items)
                   r.draw(g, 10, 10);
                points.get(poiListDep.getSelectedItem()).draw(g, 10, 10);
                g.drawString(((PointOfInterest)points.get(poiListDep.getSelectedItem())).getDescription(),
                                points.get(poiListDep.getSelectedItem()).getX(), 
                                points.get(poiListDep.getSelectedItem()).getY());
                g.drawString("Departure", result.departure.getX(), result.departure.getY());
                
                points.get(poiListDest.getSelectedItem()).draw(g, 10, 10);
                g.drawString("Destination", result.destination.getX(), result.destination.getY());
                g.drawString(((PointOfInterest)points.get(poiListDest.getSelectedItem())).getDescription(),
                                points.get(poiListDest.getSelectedItem()).getX(), 
                                points.get(poiListDest.getSelectedItem()).getY());
                
                Time dest = new Time(result.destHour, result.destMin);
                String time ="Time: " + dest.toString();
                if(!poiH.getText().equals(""))
                {
                    g.setColor(new Color(255, 215, 0));
                    g.fillRect(130, 9, 70, 15);
                    g.setColor(Color.black);
                    g.drawString(time, 130, 20);
                }
                                
                Color bfd = g.getColor();
                try{
                g.setColor(polyColor.get(polyList.get(result.line.name)));
                g.fillPolygon(polyList.get(result.line.name));
                Polygon currentPolygon = generatePolygon();
                if (currentPolygon != null)
                    g.fillPolygon(currentPolygon);}
                    catch(Exception e){}
                g.setColor(bfd);
            }
        };
        queryMap.setBorder(new TitledBorder("Map"));
        queryFrame.add(queryDescription, BorderLayout.WEST);
        queryFrame.add(queryMap, BorderLayout.CENTER);
        queryFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        queryFrame.setSize(500,300);
        queryFrame.setVisible(true);
    }
    
    private void doAction()
    {
        String actionCommand = rtsItem + actionMode;
        
        if(actionCommand.equals("BusStopNew"))
        {
            if(timePoint.isSelected())
            {
                TimePoint tp = new TimePoint(name.getText(), Integer.parseInt(x.getText()), Integer.parseInt(y.getText()));
                tp.setImage(tpImage);
                points.put(name.getText(), tp);
                busStopListDep.addItem(name.getText());
                busStopListDest.addItem(name.getText());
                busStopList.addItem(name.getText());
                busstops.put(name.getText(), tp);
            }
            else
            {
                BusStop bs = new BusStop(name.getText(), Integer.parseInt(x.getText()), Integer.parseInt(y.getText()));
                bs.setImage(bsImage);
                points.put(name.getText(), bs);
                busStopList.addItem(name.getText());
                busstops.put(name.getText(), bs);
            }
        }
        else if(actionCommand.equals("BusStopShow")) 
        {
            drawn.put((String)busStopList.getSelectedItem(), points.get(busStopList.getSelectedItem()));          
            drawingPanel.repaint();
        }
        else if(actionCommand.equals("BusStopHide"))
        {
            drawn.remove((String)busStopList.getSelectedItem());
            drawingPanel.repaint();
        }
        else if(actionCommand.equals("BusStopMove"))
        {
            points.get(busStopList.getSelectedItem()).setX(Integer.parseInt(x.getText()));
            points.get(busStopList.getSelectedItem()).setY(Integer.parseInt(y.getText()));
            drawingPanel.repaint();
        }
        else if(actionCommand.equals("BusStopWrite Line Info"))
        {
            System.out.println(((BusStop)points.get(busStopList.getSelectedItem())).getLineInfo());
            JOptionPane.showMessageDialog(busStopList, ((BusStop)points.get(busStopList.getSelectedItem())).getLineInfo());
        }
        else if(actionCommand.equals("LineNew"))
        {
            Location departure = points.get(busStopListDep.getSelectedItem());
            Location destination = points.get(busStopListDep.getSelectedItem());
            
            Line l = new Line(name.getText(), departure, destination, 
            new Time(Integer.parseInt(firstH.getText()), Integer.parseInt(firstM.getText())), 
            new Time(Integer.parseInt(lastH.getText()), Integer.parseInt(lastM.getText())),
            Integer.parseInt(period.getText()));
            
            l.addNextBusStop((TimePoint)departure, 0);   
            
            lines.put(name.getText(), l);
            lineList.addItem(name.getText());
            lineList2.addItem(name.getText());
        }
        else if(actionCommand.equals("LineAdd BusStop"))
        {
            if(points.get(busStopList.getSelectedItem()) instanceof TimePoint)
            {
                (lines.get(lineList.getSelectedItem())).addNextBusStop((TimePoint)points.get(busStopList.getSelectedItem()),
                                                                 Integer.parseInt(duration.getText()));   
            }
            else
            {
                (lines.get(lineList.getSelectedItem())).addNextBusStop((BusStop)points.get(busStopList.getSelectedItem()), 0);                                                     
            }
           
            int busStopNumber = (lines.get(lineList.getSelectedItem())).route.size()-2;
            if(busStopNumber >= 0)
            {
                String addition = "Will be added after " + (lines.get(lineList.getSelectedItem())).route.get(busStopNumber).name;
                JOptionPane.showMessageDialog(busStopList, addition);
            }
        }
        else if(actionCommand.equals("LineWrite Schedule"))
        {
            panel.removeAll();
            System.out.println(lines.get(lineList.getSelectedItem()).getSchedule());
            sched = new JTextArea(lines.get(lineList.getSelectedItem()).getSchedule());
            panel.add(sched);
            update.addActionListener((ActionListener)(this));
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(250,300); 
            frame.setVisible(true);
        }
        else if(actionCommand.equals("PointOfInterestNew"))
        {
            PointOfInterest poi = new PointOfInterest(name.getText(), Integer.parseInt(x.getText()), Integer.parseInt(y.getText()),
                                                      description.getText());
            poi.setImage(poiImage);
            points.put(name.getText(), poi);
            poiList.addItem(name.getText());
            poiListDep.addItem(name.getText());
            poiListDest.addItem(name.getText());
        }
        else if(actionCommand.equals("PointOfInterestShow"))
        {
            drawn.put((String)poiList.getSelectedItem(), points.get(poiList.getSelectedItem()));          
            drawingPanel.repaint();
        }
        else if(actionCommand.equals("PointOfInterestHide"))
        {
            drawn.remove((String)poiList.getSelectedItem());          
            drawingPanel.repaint();
        }
        else if(actionCommand.equals("PointOfInterestMove"))
        {
            points.get(poiList.getSelectedItem()).setX(Integer.parseInt(x.getText()));
            points.get(poiList.getSelectedItem()).setY(Integer.parseInt(y.getText()));
            drawingPanel.repaint();
        }
        updateCustomComponent();
    }
    
    /**
     *  Demonstrates use of various GUI elements and event handling via Listeners.
     */
    public static void main(String[] args) 
    {
        MapApp myapp = new MapApp("The BusStop on the Map!"); 
        myapp.setSize(650, 400);
        myapp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myapp.setVisible(true);
        myapp.updateCustomComponent();
        myapp.setResizable(false);
    }
}
