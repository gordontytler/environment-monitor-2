
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "logon", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "logon", namespace = "http://MonitorService", propOrder = {
    "host",
    "hostName",
    "environmentName"
})
public class Logon {

    @XmlElement(name = "host", namespace = "")
    private String host;
    @XmlElement(name = "hostName", namespace = "")
    private String hostName;
    @XmlElement(name = "environmentName", namespace = "")
    private String environmentName;

    /**
     * 
     * @return
     *     returns String
     */
    public String getHost() {
        return this.host;
    }

    /**
     * 
     * @param host
     *     the value for the host property
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * 
     * @return
     *     returns String
     */
    public String getHostName() {
        return this.hostName;
    }

    /**
     * 
     * @param hostName
     *     the value for the hostName property
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

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

}
