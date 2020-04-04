
package monitorservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for action complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="action">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="command" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="outputName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="scriptFile" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="selectedByDefault" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "action", propOrder = {
    "command",
    "outputName",
    "scriptFile",
    "selectedByDefault"
})
public class Action {

    protected boolean command;
    protected String outputName;
    protected String scriptFile;
    protected boolean selectedByDefault;

    /**
     * Gets the value of the command property.
     * 
     */
    public boolean isCommand() {
        return command;
    }

    /**
     * Sets the value of the command property.
     * 
     */
    public void setCommand(boolean value) {
        this.command = value;
    }

    /**
     * Gets the value of the outputName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOutputName() {
        return outputName;
    }

    /**
     * Sets the value of the outputName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOutputName(String value) {
        this.outputName = value;
    }

    /**
     * Gets the value of the scriptFile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScriptFile() {
        return scriptFile;
    }

    /**
     * Sets the value of the scriptFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScriptFile(String value) {
        this.scriptFile = value;
    }

    /**
     * Gets the value of the selectedByDefault property.
     * 
     */
    public boolean isSelectedByDefault() {
        return selectedByDefault;
    }

    /**
     * Sets the value of the selectedByDefault property.
     * 
     */
    public void setSelectedByDefault(boolean value) {
        this.selectedByDefault = value;
    }

}
