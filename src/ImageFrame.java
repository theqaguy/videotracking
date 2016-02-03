import java.awt.*;
import java.awt.event.*;

public class ImageFrame extends Frame{

	public boolean isOpen = false;
	
	  public ImageFrame (String title) 
	  {
	    setTitle(title);  // Fenstertitel setzen
	    setSize(400,100);                            // Fenstergr��e einstellen  
	    addWindowListener(new TestWindowListener()); // EventListener f�r das Fenster hinzuf�gen
	                                                 // (notwendig, damit das Fenster geschlossen werden kann)
	    setVisible(true);                            // Fenster (inkl. Inhalt) sichtbar machen
	    isOpen = true;
	  }

	  class TestWindowListener extends WindowAdapter
	  {
	    public void windowClosing(WindowEvent e)
	    {
	      e.getWindow().dispose();                   // Fenster "killen"
	      isOpen = false;
	    }           
	  }
}
