
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "close", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "close", namespace = "http://MonitorService")
public class Close {

    @XmlElement(name = "sessionId", namespace = "")
    private String sessionId;

    /**
     * 
     * @return
     *     returns String
     */
    public String getSessionId() {
        return this.sessionId;
    }

    /**
     * 
     * @param sessionId
     *     the value for the sessionId property
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

}
