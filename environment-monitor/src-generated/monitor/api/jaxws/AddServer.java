
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "addServer", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addServer", namespace = "http://MonitorService", propOrder = {
    "environmentName",
    "serverName"
})
public class AddServer {

    @XmlElement(name = "environmentName", namespace = "")
    private String environmentName;
    @XmlElement(name = "serverName", namespace = "")
    private String serverName;

    /**
     * 
     * @return
     *     returns String
     */
    public String getEnvironmentName() {
        return this.environmentName;
    }

    /**
     * 
     * @param environmentName
     *     the value for the environmentName property
     */
    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    /**
     * 
     * @return
     *     returns String
     */
    public String getServerName() {
        return this.serverName;
    }

    /**
     * 
     * @param serverName
     *     the value for the serverName property
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

}
