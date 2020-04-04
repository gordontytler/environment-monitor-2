
package monitorservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for outputHistory complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="outputHistory">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bytes" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="lines" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="severity" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "outputHistory", propOrder = {
    "bytes",
    "lines",
    "severity"
})
public class OutputHistory {

    protected int bytes;
    protected int lines;
    protected int severity;

    /**
     * Gets the value of the bytes property.
     * 
     */
    public int getBytes() {
        return bytes;
    }

    /**
     * Sets the value of the bytes property.
     * 
     */
    public void setBytes(int value) {
        this.bytes = value;
    }

    /**
     * Gets the value of the lines property.
     * 
     */
    public int getLines() {
        return lines;
    }

    /**
     * Sets the value of the lines property.
     * 
     */
    public void setLines(int value) {
        this.lines = value;
    }

    /**
     * Gets the value of the severity property.
     * 
     */
    public int getSeverity() {
        return severity;
    }

    /**
     * Sets the value of the severity property.
     * 
     */
    public void setSeverity(int value) {
        this.severity = value;
    }

}
