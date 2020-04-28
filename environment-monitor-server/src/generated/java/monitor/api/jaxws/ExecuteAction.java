
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "executeAction", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "executeAction", namespace = "http://MonitorService", propOrder = {
    "environmentName",
    "environmentViewRow"
})
public class ExecuteAction {

    @XmlElement(name = "environmentName", namespace = "")
    private String environmentName;
    @XmlElement(name = "environmentViewRow", namespace = "")
    private monitor.model.EnvironmentViewRow environmentViewRow;

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
     *     returns EnvironmentViewRow
     */
    public monitor.model.EnvironmentViewRow getEnvironmentViewRow() {
        return this.environmentViewRow;
    }

    /**
     * 
     * @param environmentViewRow
     *     the value for the environmentViewRow property
     */
    public void setEnvironmentViewRow(monitor.model.EnvironmentViewRow environmentViewRow) {
        this.environmentViewRow = environmentViewRow;
    }

}
