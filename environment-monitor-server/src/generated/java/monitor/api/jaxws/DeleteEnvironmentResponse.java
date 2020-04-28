
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "deleteEnvironmentResponse", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "deleteEnvironmentResponse", namespace = "http://MonitorService")
public class DeleteEnvironmentResponse {

    @XmlElement(name = "deleteEnvironmentResult", namespace = "")
    private monitor.model.CommandResult deleteEnvironmentResult;

    /**
     * 
     * @return
     *     returns CommandResult
     */
    public monitor.model.CommandResult getDeleteEnvironmentResult() {
        return this.deleteEnvironmentResult;
    }

    /**
     * 
     * @param deleteEnvironmentResult
     *     the value for the deleteEnvironmentResult property
     */
    public void setDeleteEnvironmentResult(monitor.model.CommandResult deleteEnvironmentResult) {
        this.deleteEnvironmentResult = deleteEnvironmentResult;
    }

}
