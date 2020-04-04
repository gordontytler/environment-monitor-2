
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "saveEnvironmentResponse", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "saveEnvironmentResponse", namespace = "http://MonitorService")
public class SaveEnvironmentResponse {

    @XmlElement(name = "saveEnvironmentResult", namespace = "")
    private monitor.model.CommandResult saveEnvironmentResult;

    /**
     * 
     * @return
     *     returns CommandResult
     */
    public monitor.model.CommandResult getSaveEnvironmentResult() {
        return this.saveEnvironmentResult;
    }

    /**
     * 
     * @param saveEnvironmentResult
     *     the value for the saveEnvironmentResult property
     */
    public void setSaveEnvironmentResult(monitor.model.CommandResult saveEnvironmentResult) {
        this.saveEnvironmentResult = saveEnvironmentResult;
    }

}
