
package monitor.api.jaxws;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "getEnvironmentNamesResponse", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getEnvironmentNamesResponse", namespace = "http://MonitorService")
public class GetEnvironmentNamesResponse {

    @XmlElement(name = "environmentNames", namespace = "")
    private List<String> environmentNames;

    /**
     * 
     * @return
     *     returns List<String>
     */
    public List<String> getEnvironmentNames() {
        return this.environmentNames;
    }

    /**
     * 
     * @param environmentNames
     *     the value for the environmentNames property
     */
    public void setEnvironmentNames(List<String> environmentNames) {
        this.environmentNames = environmentNames;
    }

}
