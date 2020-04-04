
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "deleteRowResponse", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "deleteRowResponse", namespace = "http://MonitorService")
public class DeleteRowResponse {

    @XmlElement(name = "deleteRowResult", namespace = "")
    private monitor.model.CommandResult deleteRowResult;

    /**
     * 
     * @return
     *     returns CommandResult
     */
    public monitor.model.CommandResult getDeleteRowResult() {
        return this.deleteRowResult;
    }

    /**
     * 
     * @param deleteRowResult
     *     the value for the deleteRowResult property
     */
    public void setDeleteRowResult(monitor.model.CommandResult deleteRowResult) {
        this.deleteRowResult = deleteRowResult;
    }

}
