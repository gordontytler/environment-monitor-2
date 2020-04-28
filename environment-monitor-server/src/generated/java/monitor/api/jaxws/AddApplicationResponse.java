
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "addApplicationResponse", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addApplicationResponse", namespace = "http://MonitorService")
public class AddApplicationResponse {

    @XmlElement(name = "addApplicationResult", namespace = "")
    private monitor.model.CommandResult addApplicationResult;

    /**
     * 
     * @return
     *     returns CommandResult
     */
    public monitor.model.CommandResult getAddApplicationResult() {
        return this.addApplicationResult;
    }

    /**
     * 
     * @param addApplicationResult
     *     the value for the addApplicationResult property
     */
    public void setAddApplicationResult(monitor.model.CommandResult addApplicationResult) {
        this.addApplicationResult = addApplicationResult;
    }

}
