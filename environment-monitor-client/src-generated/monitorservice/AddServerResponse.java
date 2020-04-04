
package monitorservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for addServerResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="addServerResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="addServerResult" type="{http://MonitorService}commandResult" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addServerResponse", propOrder = {
    "addServerResult"
})
public class AddServerResponse {

    protected CommandResult addServerResult;

    /**
     * Gets the value of the addServerResult property.
     * 
     * @return
     *     possible object is
     *     {@link CommandResult }
     *     
     */
    public CommandResult getAddServerResult() {
        return addServerResult;
    }

    /**
     * Sets the value of the addServerResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommandResult }
     *     
     */
    public void setAddServerResult(CommandResult value) {
        this.addServerResult = value;
    }

}
