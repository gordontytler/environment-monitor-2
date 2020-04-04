
package monitorservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for addApplicationResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="addApplicationResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="addApplicationResult" type="{http://MonitorService}commandResult" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addApplicationResponse", propOrder = {
    "addApplicationResult"
})
public class AddApplicationResponse {

    protected CommandResult addApplicationResult;

    /**
     * Gets the value of the addApplicationResult property.
     * 
     * @return
     *     possible object is
     *     {@link CommandResult }
     *     
     */
    public CommandResult getAddApplicationResult() {
        return addApplicationResult;
    }

    /**
     * Sets the value of the addApplicationResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommandResult }
     *     
     */
    public void setAddApplicationResult(CommandResult value) {
        this.addApplicationResult = value;
    }

}
