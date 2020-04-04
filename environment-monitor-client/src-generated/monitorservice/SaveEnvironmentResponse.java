
package monitorservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for saveEnvironmentResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="saveEnvironmentResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="saveEnvironmentResult" type="{http://MonitorService}commandResult" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "saveEnvironmentResponse", propOrder = {
    "saveEnvironmentResult"
})
public class SaveEnvironmentResponse {

    protected CommandResult saveEnvironmentResult;

    /**
     * Gets the value of the saveEnvironmentResult property.
     * 
     * @return
     *     possible object is
     *     {@link CommandResult }
     *     
     */
    public CommandResult getSaveEnvironmentResult() {
        return saveEnvironmentResult;
    }

    /**
     * Sets the value of the saveEnvironmentResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommandResult }
     *     
     */
    public void setSaveEnvironmentResult(CommandResult value) {
        this.saveEnvironmentResult = value;
    }

}
