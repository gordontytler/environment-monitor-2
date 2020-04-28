
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "addServerResponse", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addServerResponse", namespace = "http://MonitorService")
public class AddServerResponse {

    @XmlElement(name = "addServerResult", namespace = "")
    private monitor.model.CommandResult addServerResult;

    /**
     * 
     * @return
     *     returns CommandResult
     */
    public monitor.model.CommandResult getAddServerResult() {
        return this.addServerResult;
    }

    /**
     * 
     * @param addServerResult
     *     the value for the addServerResult property
     */
    public void setAddServerResult(monitor.model.CommandResult addServerResult) {
        this.addServerResult = addServerResult;
    }

}
