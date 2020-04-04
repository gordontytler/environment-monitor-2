package monitor.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class PictureRenderer extends JButton implements TableCellRenderer {
    //Border unselectedBorder = null;
    //Border selectedBorder = null;
    //boolean isBordered = true;

	// Used by PictureRenderer to find an existing PictureIcon that wants to append some ListOfOutputHistory
	private HashMap<PictureIconKey, PictureIcon> pictureIconMap = new HashMap<PictureIconKey, PictureIcon>();

    Color background;
    Color selectionBackground;    
    
    public PictureRenderer(Color background, Color selectionBackground) {
    	this.background = background;
    	this.selectionBackground = selectionBackground;
        setOpaque(true);
        setContentAreaFilled(false);
        // this.disableEvents(AWTEvent.PAINT_EVENT_MASK); <- has no effect
    }

    @Override
    public Component getTableCellRendererComponent(
                            JTable table, Object tableObject,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) {
    	ListOfOutputHistory historiesWrapper = (ListOfOutputHistory)tableObject;
        setBackground(isSelected ? selectionBackground : background);
        PictureIconKey key = historiesWrapper.getPictureIconKey();
    	PictureIcon icon = pictureIconMap.get(key);
        if (icon == null ) {    		
    		icon = new PictureIcon();
    		pictureIconMap.put(key, icon);
    	}
    	Rectangle cellRect = table.getCellRect(row, column, true);
    	icon.setIconWidth(cellRect.width);
    	icon.setIconHeight(cellRect.height);
        icon.setBackground(isSelected ? selectionBackground : background);
        icon.setHistories(historiesWrapper.getOutputHistories());
    	setIcon(icon);
    	//setToolTipText("123 exceptions 65 lines per minute");
        return this;
    }
}
