
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "renameEnvironment", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "renameEnvironment", namespace = "http://MonitorService", propOrder = {
    "oldName",
    "newName"
})
public class RenameEnvironment {

    @XmlElement(name = "oldName", namespace = "")
    private String oldName;
    @XmlElement(name = "newName", namespace = "")
    private String newName;

    /**
     * 
     * @return
     *     returns String
     */
    public String getOldName() {
        return this.oldName;
    }

    /**
     * 
     * @param oldName
     *     the value for the oldName property
     */
    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    /**
     * 
     * @return
     *     returns String
     */
    public String getNewName() {
        return this.newName;
    }

    /**
     * 
     * @param newName
     *     the value for the newName property
     */
    public void setNewName(String newName) {
        this.newName = newName;
    }

}
