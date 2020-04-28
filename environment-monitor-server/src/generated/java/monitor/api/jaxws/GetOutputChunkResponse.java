
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "getOutputChunkResponse", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getOutputChunkResponse", namespace = "http://MonitorService")
public class GetOutputChunkResponse {

    @XmlElement(name = "return", namespace = "")
    private monitor.model.OutputChunkResult _return;

    /**
     * 
     * @return
     *     returns OutputChunkResult
     */
    public monitor.model.OutputChunkResult getReturn() {
        return this._return;
    }

    /**
     * 
     * @param _return
     *     the value for the _return property
     */
    public void setReturn(monitor.model.OutputChunkResult _return) {
        this._return = _return;
    }

}
