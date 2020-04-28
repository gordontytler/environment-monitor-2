
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "logonResponse", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "logonResponse", namespace = "http://MonitorService")
public class LogonResponse {

    @XmlElement(name = "loginResult", namespace = "")
    private monitor.model.LogonResult loginResult;

    /**
     * 
     * @return
     *     returns LogonResult
     */
    public monitor.model.LogonResult getLoginResult() {
        return this.loginResult;
    }

    /**
     * 
     * @param loginResult
     *     the value for the loginResult property
     */
    public void setLoginResult(monitor.model.LogonResult loginResult) {
        this.loginResult = loginResult;
    }

}
