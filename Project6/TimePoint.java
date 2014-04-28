import java.awt.Image;
import java.awt.Graphics;

/** Class that represents a bus stop that is associated with a time point in the line schedule */
public class TimePoint extends BusStop
{
    protected static Image image;
    private final static long serialVersionUID = 100;
    
    public TimePoint()
    {     
    }
  
    /** Initializes name, x, and y using the parameters by calling super*/ 
    public TimePoint(String name, int x, int y)
    {
        super(name, x, y);
    }

    /** Initializes name, x, and y, and lines using the parameters by calling super*/ 
    public TimePoint(String name, int x, int y, Line[] lines, int numLines)
    {
        super(name, x, y, lines, numLines);
    }
  
    public void setImage(Image image)
    {
        this.image = image;
    }
    
    public Image getImage()
    {
        return image;
    }
    
    public void draw(Graphics g, int width, int height)
    {
        g.drawImage(image, x, y, width, height, null);
    }
}
