
package monitorservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for outputInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="outputInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="commandStatus" type="{http://MonitorService}commandStatus" minOccurs="0"/>
 *         &lt;element name="highestChunk" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="lowestChunk" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="sessionId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "outputInfo", propOrder = {
    "commandStatus",
    "highestChunk",
    "lowestChunk",
    "sessionId"
})
@XmlSeeAlso({
    OutputChunkResult.class,
    EnvironmentViewRow.class
})
public class OutputInfo {

    protected CommandStatus commandStatus;
    protected int highestChunk;
    protected int lowestChunk;
    protected String sessionId;

    /**
     * Gets the value of the commandStatus property.
     * 
     * @return
     *     possible object is
     *     {@link CommandStatus }
     *     
     */
    public CommandStatus getCommandStatus() {
        return commandStatus;
    }

    /**
     * Sets the value of the commandStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommandStatus }
     *     
     */
    public void setCommandStatus(CommandStatus value) {
        this.commandStatus = value;
    }

    /**
     * Gets the value of the highestChunk property.
     * 
     */
    public int getHighestChunk() {
        return highestChunk;
    }

    /**
     * Sets the value of the highestChunk property.
     * 
     */
    public void setHighestChunk(int value) {
        this.highestChunk = value;
    }

    /**
     * Gets the value of the lowestChunk property.
     * 
     */
    public int getLowestChunk() {
        return lowestChunk;
    }

    /**
     * Sets the value of the lowestChunk property.
     * 
     */
    public void setLowestChunk(int value) {
        this.lowestChunk = value;
    }

    /**
     * Gets the value of the sessionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the value of the sessionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSessionId(String value) {
        this.sessionId = value;
    }

}
