
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "executeCommandResponse", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "executeCommandResponse", namespace = "http://MonitorService")
public class ExecuteCommandResponse {

    @XmlElement(name = "return", namespace = "")
    private monitor.model.CommandResult _return;

    /**
     * 
     * @return
     *     returns CommandResult
     */
    public monitor.model.CommandResult getReturn() {
        return this._return;
    }

    /**
     * 
     * @param _return
     *     the value for the _return property
     */
    public void setReturn(monitor.model.CommandResult _return) {
        this._return = _return;
    }

}
