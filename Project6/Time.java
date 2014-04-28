/** 24-Hour Time */
public class Time extends Object implements java.io.Serializable
{
    private int hours;  
    private int minutes;
    private final static long serialVersionUID = 100;
      
    /** Sets time to 0:00 (12 midnight)*/
    public Time() 
    {
        hours = 0;
        minutes = 0;
    }
           
    /** Initializes this to the given time */
    public Time(int hours, int minutes) 
    {
        this.hours = hours;
        this.minutes = minutes;
    }   
               
    /** Set this to the given time */           
    public void set(int hours, int minutes)
    {
        this.hours = hours;
        this.minutes = minutes;
    }
                 
    /** Get the hour */              
    public int getHours() 
    {
        return hours; 
    }
                   
    /** Get the minutes */               
    public int getMinutes()
    {
        return minutes; 
    }
                       
    /** Return a Time object that is minutes ahead of this time */
    public Time advanceMinutes(int minutes) 
    { 
       return new Time((this.hours + (this.minutes + minutes)/60) % 24, 
                        (this.minutes + minutes) % 60);         
    }
                         
    public String toString() 
    { 
        return (hours < 10 ? "0" : "") + hours + ":" + (minutes < 10 ? "0" : "")
                 + minutes;
    }
    
    public Time clone()
    {
        Time clone = new Time(hours, minutes);
        return clone;
    }
    
    public boolean equals(Time time)
    {
        if(time.minutes != minutes || time.hours != hours)
            return false;
        else
            return true;
    }
    
    public int compareTo(Time time)
    {
        if (equals(time))
            return 0;
        else 
        {
            if (hours < time.hours || (hours == time.hours && minutes < time.minutes))
                 return -1;
            else 
                return 1;
        }
    }
}