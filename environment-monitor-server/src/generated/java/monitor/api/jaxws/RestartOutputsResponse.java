
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "restartOutputsResponse", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "restartOutputsResponse", namespace = "http://MonitorService")
public class RestartOutputsResponse {

    @XmlElement(name = "restartOutputsResult", namespace = "")
    private monitor.model.CommandResult restartOutputsResult;

    /**
     * 
     * @return
     *     returns CommandResult
     */
    public monitor.model.CommandResult getRestartOutputsResult() {
        return this.restartOutputsResult;
    }

    /**
     * 
     * @param restartOutputsResult
     *     the value for the restartOutputsResult property
     */
    public void setRestartOutputsResult(monitor.model.CommandResult restartOutputsResult) {
        this.restartOutputsResult = restartOutputsResult;
    }

}
