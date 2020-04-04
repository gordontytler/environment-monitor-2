
package monitorservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for outputChunkResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="outputChunkResult">
 *   &lt;complexContent>
 *     &lt;extension base="{http://MonitorService}outputInfo">
 *       &lt;sequence>
 *         &lt;element name="chunkNumber" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="output" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "outputChunkResult", propOrder = {
    "chunkNumber",
    "output"
})
public class OutputChunkResult
    extends OutputInfo
{

    protected int chunkNumber;
    protected String output;

    /**
     * Gets the value of the chunkNumber property.
     * 
     */
    public int getChunkNumber() {
        return chunkNumber;
    }

    /**
     * Sets the value of the chunkNumber property.
     * 
     */
    public void setChunkNumber(int value) {
        this.chunkNumber = value;
    }

    /**
     * Gets the value of the output property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOutput() {
        return output;
    }

    /**
     * Sets the value of the output property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOutput(String value) {
        this.output = value;
    }

}
