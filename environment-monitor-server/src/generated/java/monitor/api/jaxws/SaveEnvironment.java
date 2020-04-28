
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "saveEnvironment", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "saveEnvironment", namespace = "http://MonitorService")
public class SaveEnvironment {

    @XmlElement(name = "environmentName", namespace = "")
    private String environmentName;

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
