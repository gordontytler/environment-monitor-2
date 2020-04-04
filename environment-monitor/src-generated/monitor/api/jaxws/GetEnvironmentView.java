
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "getEnvironmentView", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getEnvironmentView", namespace = "http://MonitorService", propOrder = {
    "environmentName",
    "outputHistoryTimeStamp"
})
public class GetEnvironmentView {

    @XmlElement(name = "environmentName", namespace = "")
    private String environmentName;
    @XmlElement(name = "outputHistoryTimeStamp", namespace = "")
    private long outputHistoryTimeStamp;

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
     *     returns long
     */
    public long getOutputHistoryTimeStamp() {
        return this.outputHistoryTimeStamp;
    }

    /**
     * 
     * @param outputHistoryTimeStamp
     *     the value for the outputHistoryTimeStamp property
     */
    public void setOutputHistoryTimeStamp(long outputHistoryTimeStamp) {
        this.outputHistoryTimeStamp = outputHistoryTimeStamp;
    }

}
