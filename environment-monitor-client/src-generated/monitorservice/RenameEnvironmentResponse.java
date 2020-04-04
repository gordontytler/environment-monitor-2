
package monitorservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for renameEnvironmentResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="renameEnvironmentResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="renameEnvironmentResult" type="{http://MonitorService}commandResult" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "renameEnvironmentResponse", propOrder = {
    "renameEnvironmentResult"
})
public class RenameEnvironmentResponse {

    protected CommandResult renameEnvironmentResult;

    /**
     * Gets the value of the renameEnvironmentResult property.
     * 
     * @return
     *     possible object is
     *     {@link CommandResult }
     *     
     */
    public CommandResult getRenameEnvironmentResult() {
        return renameEnvironmentResult;
    }

    /**
     * Sets the value of the renameEnvironmentResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommandResult }
     *     
     */
    public void setRenameEnvironmentResult(CommandResult value) {
        this.renameEnvironmentResult = value;
    }

}
