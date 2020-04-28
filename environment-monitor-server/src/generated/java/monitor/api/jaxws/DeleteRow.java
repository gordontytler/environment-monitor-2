
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "deleteRow", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "deleteRow", namespace = "http://MonitorService", propOrder = {
    "environmentName",
    "index"
})
public class DeleteRow {

    @XmlElement(name = "environmentName", namespace = "")
    private String environmentName;
    @XmlElement(name = "index", namespace = "")
    private int index;

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
     *     returns int
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * 
     * @param index
     *     the value for the index property
     */
    public void setIndex(int index) {
        this.index = index;
    }

}
