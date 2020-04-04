
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "renameEnvironmentResponse", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "renameEnvironmentResponse", namespace = "http://MonitorService")
public class RenameEnvironmentResponse {

    @XmlElement(name = "renameEnvironmentResult", namespace = "")
    private monitor.model.CommandResult renameEnvironmentResult;

    /**
     * 
     * @return
     *     returns CommandResult
     */
    public monitor.model.CommandResult getRenameEnvironmentResult() {
        return this.renameEnvironmentResult;
    }

    /**
     * 
     * @param renameEnvironmentResult
     *     the value for the renameEnvironmentResult property
     */
    public void setRenameEnvironmentResult(monitor.model.CommandResult renameEnvironmentResult) {
        this.renameEnvironmentResult = renameEnvironmentResult;
    }

}
