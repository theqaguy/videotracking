import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SettingsFrame extends Frame implements ChangeListener{

	public boolean isOpen = false;
	public JSlider hminTrackbar = null;
	public JSlider sminTrackbar = null;
	public JSlider vminTrackbar = null;
	public JSlider hmaxTrackbar = null;
	public JSlider smaxTrackbar = null;
	public JSlider vmaxTrackbar = null;
	private JLabel framerateLabel = null;
	private JLabel coordinateLabel = null;
	public JCheckBox morphCheckbox;
	public JCheckBox trackCheckbox;
	public JCheckBox feedCheckbox;
	private int MIN = 0;
	private int MAX = 256;
	
	  public SettingsFrame () 
	  {
	    setTitle("Settings");  // Fenstertitel setzen
	    setSize(600,600);                            // Fenstergröße einstellen  
	    addWindowListener(new TestWindowListener()); // EventListener für das Fenster hinzufügen
	                                                 // (notwendig, damit das Fenster geschlossen werden kann)
	    setVisible(true);                            // Fenster (inkl. Inhalt) sichtbar machen
	    isOpen = true;
	    
	    hminTrackbar = createTrackbar(MIN);
	    sminTrackbar = createTrackbar(MIN);
	    vminTrackbar = createTrackbar(MIN);
	    hmaxTrackbar = createTrackbar(MAX);
	    smaxTrackbar = createTrackbar(MAX);
	    vmaxTrackbar = createTrackbar(MAX);
	    framerateLabel = new JLabel("0");
	    coordinateLabel = new JLabel("");
	    morphCheckbox = new JCheckBox();
	    morphCheckbox.setSelected(false);
	    trackCheckbox = new JCheckBox();
	    trackCheckbox.setSelected(false);
	    feedCheckbox = new JCheckBox();
	    feedCheckbox.setSelected(true);
	    setLayout(new GridLayout(8,3));
	    // add trackbars for color separation
	    add(new JLabel("H"));
	    add(hminTrackbar);
	    add(hmaxTrackbar);
	    add(new JLabel("S"));
	    add(sminTrackbar);
	    add(smaxTrackbar);
	    add(new JLabel("V"));
	    add(vminTrackbar);
	    add(vmaxTrackbar);
	    // add checkboxes for processing
	    add(new JLabel("useMorphOps"));
	    add(morphCheckbox);
	    add(new JLabel(""));
	    add(new JLabel("trackObjects"));
	    add(trackCheckbox);
	    add(new JLabel(""));
	    add(new JLabel("show feeds"));
	    add(feedCheckbox);
	    add(new JLabel(""));
	    // add coordinate display
	    add(new JLabel("Coordinates:"));
	    add(coordinateLabel);
	    add(new JLabel(""));
	    // add framerate display
	    add(new JLabel("Framerate:"));
	    add(framerateLabel);
	    add(new JLabel("fps"));
	    
	  }
	  
	  private JSlider createTrackbar(int startValue) {
		  JSlider result = new JSlider(JSlider.HORIZONTAL,
                  MIN, MAX, startValue);
		  result.setMajorTickSpacing(20);
		  result.setPaintTicks(true);
		  result.setPaintLabels(true);
		  
//		  result.addChangeListener(this);
		  
		  return result;
	  }
	  
	  public void setFramerate(long frames) {
		  framerateLabel.setText(String.valueOf(frames));
	  }
	  
	  public void setCoordinates(String coords) {
		  coordinateLabel.setText(coords);
	  }

	  class TestWindowListener extends WindowAdapter
	  {
	    public void windowClosing(WindowEvent e)
	    {
	      e.getWindow().dispose();                   // Fenster "killen"
	      System.exit(0);
	    }           
	  }

	@Override
	public void stateChanged(ChangeEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
