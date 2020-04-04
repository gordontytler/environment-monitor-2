
package monitorservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for executeAction complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="executeAction">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="environmentName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="environmentViewRow" type="{http://MonitorService}environmentViewRow" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "executeAction", propOrder = {
    "environmentName",
    "environmentViewRow"
})
public class ExecuteAction {

    protected String environmentName;
    protected EnvironmentViewRow environmentViewRow;

    /**
     * Gets the value of the environmentName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnvironmentName() {
        return environmentName;
    }

    /**
     * Sets the value of the environmentName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnvironmentName(String value) {
        this.environmentName = value;
    }

    /**
     * Gets the value of the environmentViewRow property.
     * 
     * @return
     *     possible object is
     *     {@link EnvironmentViewRow }
     *     
     */
    public EnvironmentViewRow getEnvironmentViewRow() {
        return environmentViewRow;
    }

    /**
     * Sets the value of the environmentViewRow property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnvironmentViewRow }
     *     
     */
    public void setEnvironmentViewRow(EnvironmentViewRow value) {
        this.environmentViewRow = value;
    }

}
