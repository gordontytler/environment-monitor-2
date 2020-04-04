
package monitorservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for deleteEnvironmentResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="deleteEnvironmentResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="deleteEnvironmentResult" type="{http://MonitorService}commandResult" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "deleteEnvironmentResponse", propOrder = {
    "deleteEnvironmentResult"
})
public class DeleteEnvironmentResponse {

    protected CommandResult deleteEnvironmentResult;

    /**
     * Gets the value of the deleteEnvironmentResult property.
     * 
     * @return
     *     possible object is
     *     {@link CommandResult }
     *     
     */
    public CommandResult getDeleteEnvironmentResult() {
        return deleteEnvironmentResult;
    }

    /**
     * Sets the value of the deleteEnvironmentResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommandResult }
     *     
     */
    public void setDeleteEnvironmentResult(CommandResult value) {
        this.deleteEnvironmentResult = value;
    }

}
