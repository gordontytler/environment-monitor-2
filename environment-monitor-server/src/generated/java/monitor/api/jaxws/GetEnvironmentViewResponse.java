
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "getEnvironmentViewResponse", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getEnvironmentViewResponse", namespace = "http://MonitorService")
public class GetEnvironmentViewResponse {

    @XmlElement(name = "return", namespace = "")
    private monitor.model.EnvironmentView _return;

    /**
     * 
     * @return
     *     returns EnvironmentView
     */
    public monitor.model.EnvironmentView getReturn() {
        return this._return;
    }

    /**
     * 
     * @param _return
     *     the value for the _return property
     */
    public void setReturn(monitor.model.EnvironmentView _return) {
        this._return = _return;
    }

}
