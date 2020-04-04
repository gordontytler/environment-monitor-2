package monitor.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.Icon;

import monitorservice.OutputHistory;

public class PictureIcon implements Icon {

	static Logger logger = Logger.getLogger(PictureIcon.class.getName());	

	private int width = 0;
	private int height = 0;
	private boolean widthChanged = false;
	
	// higher values are drawn as if they had these value - 
	// perhaps it needs to automatically rescale
	private int maxLines = 4;  // width (was 20)
	private double maxBytes = 1000; // height
	private int maxSeverity = 4; // colour

	private enum Severity {
		GREEN, YELLOWY_GREEN, YELLOW, ORANGE, RED
	}	
	/** {@link monitor.implementation.shell.HistorySeverityMarker}*/
	private Color[] severityColours = {
			new Color(52,255,51),
			new Color(204,255,0),
			new Color(255,204,0),
			new Color(255,153,51),
			new Color(255,0,0)
	};
	
	private List<OutputHistory> histories = null;
	private Color iconBackground;
	private boolean paintIconEnabled = false;
	
    public PictureIcon() {
	}
    
	public synchronized void setHistories(List<OutputHistory> outputHistories) {
		histories = outputHistories;
		paintIconEnabled = true;
		//logger.info(String.format("newHistories %d ", histories.size()));		
	}

	public boolean widthChanged() {
		return widthChanged;
	}

	public void setBackground(Color color) {
		this.iconBackground = color;
	}

	public synchronized void setIconWidth(int width) {
		if (this.width != 0 && this.width != width) {
			widthChanged = true;
		}
		this.width = width;
	}

	public synchronized void setIconHeight(int height) {
		this.height = height;
	}
	
	@Override
	public int getIconWidth() {
        return width;  // e.g. 200 but we can draw outside this
    }

	@Override
    public int getIconHeight() {
        return height;  // e.g. 18;
    }

	/** Called when table reloaded or when cell resized. Draws from the right starting with most recent history.
	 * The x axis goes from left to right, y goes from top to bottom. */
    @Override
    public synchronized void paintIcon(Component c, Graphics g, int xOrigin, int yOrigin) {

    	g.setColor(iconBackground);
        g.fillRect(xOrigin, yOrigin, getIconWidth(), getIconHeight());

        boolean debug = false;
        if (debug) {
        	drawDebugPicture(g);
			return;
        }
    	
    	if (!paintIconEnabled) {
    		return;
    	}
    	paintIconEnabled = false; // prevent painting until setHistories called
        
        int x = xOrigin + getIconWidth();
        int y = 0;
        int totalWidth = 0; 
        
		for (int h = 0; h < histories.size() && totalWidth < getIconWidth(); h++) {
        	OutputHistory history = histories.get(h);

        	double fractionOfMaxWidth = history.getLines() / maxLines;
        	int width = 1 + (int) Math.round(fractionOfMaxWidth);
        	
        	double fractionOfMaxHeight = history.getBytes() / maxBytes;
        	int height = 5 + (int) Math.round((long)(getIconHeight() - 4) * fractionOfMaxHeight);
        	//System.out.println(String.format("%d %d %f", height, history.getBytes(), fractionOfMaxHeight));
        	
        	if (height > getIconHeight()) {
        		height = getIconHeight();
        	}
        	int severity = history.getSeverity();
        	if (severity > maxSeverity) {
        		severity = maxSeverity;
        	}
        	
        	x = x - width;
        	y = yOrigin + 2 - height + (getIconHeight() - 4);
        	drawBar(x, y, width, height, g, severity);

        	x = x - 1; // a gap between bars
        	width += 1; 
        	totalWidth += width;
        }
    }

	private void drawBar(int x, int y, int width, int height, Graphics g, int severity) {
		g.setColor(severityColours[severity]);
		g.fillRect(x, y, width, height);
    	//System.out.println(String.format("drawBar(%d, %d, %d, %d, g, Severity.%s.ordinal());", x, y, width, height, severityToName(severity)));		
	}

	@SuppressWarnings("unused")
	private String severityToName(int severity) {
		switch (severity) {
		case 0 : return "GREEN";
		case 1 : return "YELLOWY_GREEN";
		case 2 : return "YELLOW";
		case 3 : return "ORANGE";
		case 4 : return "RED";
		default : return "GREEN";
		}
	}

	private void drawDebugPicture(Graphics g) {
		drawBar(235, 10, 2, 5, g, Severity.YELLOWY_GREEN.ordinal());
		drawBar(231, 10, 3, 5, g, Severity.YELLOWY_GREEN.ordinal());
		drawBar(228, 10, 2, 5, g, Severity.RED.ordinal());
		drawBar(226, 10, 1, 5, g, Severity.RED.ordinal());
		drawBar(219, -3, 6, 18, g, Severity.RED.ordinal());
		drawBar(217, 10, 1, 5, g, Severity.YELLOWY_GREEN.ordinal());
		drawBar(214, 10, 2, 5, g, Severity.YELLOWY_GREEN.ordinal());
		drawBar(210, -3, 3, 18, g, Severity.YELLOWY_GREEN.ordinal());
		return;
	}	
}
