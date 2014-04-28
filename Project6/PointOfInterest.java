import java.awt.Image;
import java.awt.Graphics;

public class PointOfInterest extends Location
{
    protected String description;
    protected static Image image;
    private final static long serialVersionUID = 100;
    
    public PointOfInterest()
    {
    }
    
    public PointOfInterest(String name, int x, int y, String description)
    {
        super(name, x, y);
        this.description = description;
    }
    
    public PointOfInterest(String name, int x, int y, String description, Line[] lines, int numLines)
    {
         super(name, x, y);
         this.description = description;
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
    
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    public String getDescription()
    {
        return description;
    }
}
