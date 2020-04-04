
package monitorservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for commandResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="commandResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="chunkNumber" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="commandStatus" type="{http://MonitorService}commandStatus" minOccurs="0"/>
 *         &lt;element name="output" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "commandResult", propOrder = {
    "chunkNumber",
    "commandStatus",
    "output",
    "sessionId"
})
public class CommandResult {

    protected int chunkNumber;
    protected CommandStatus commandStatus;
    protected String output;
    protected String sessionId;

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
