import java.awt.Image;
import java.awt.Graphics;
import java.lang.Math;
import java.io.File;

/** Class that represents a location on a 2D map */
public class Location implements Drawable, java.io.Serializable
{
    protected int x;
    protected int y;
    protected String name;
    protected static Image image;
    private final static long serialVersionUID = 100;
 
    /** Set x and y to 0 and name to empty String */
    public Location()
    {
        x = 0;
        y = 0;
        name = "";
    }
  
    public Location(String name, int x, int y)
    {
        this.name = name;
        this.x = x;
        this.y = y;
    }
  
    public String getName()
    { 
        return name; 
    }
  
    public void setName(String name)
    { 
        this.name = name;
    }
  
    public int getX()
    {
        return x;
    }

    public void setX(int x) 
    {
        this.x = x; 
    } 
    
    public int getY() 
    { 
        return y; 
    }

    public void setY(int y)
    { 
        this.y = y; 
    }
   
    /** Returns the String "name at (x,y)" */
    public String toString()
    {
        return name + " at (" + x +"," + y + ")"; 
    }

    /** Prints "name at (x,y)" */
    public void printInfo()
    {
        System.out.println(name + " at (" + x +"," + y + ")");
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
    
    public boolean covers(int x, int y, int width, int height)
    {
         return (this.x <= x && x <= this.x + width && this.y <= y && y <= this.y + height);
    }
    
    public int distanceFrom(Location other)
    {
         double xdiff = this.x - other.x;
         double ydiff = this.y - other.y;
         return (int)Math.sqrt(xdiff*xdiff + ydiff*ydiff);
    }
}
