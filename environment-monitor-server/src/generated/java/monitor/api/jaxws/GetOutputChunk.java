
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "getOutputChunk", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getOutputChunk", namespace = "http://MonitorService", propOrder = {
    "sessionId",
    "chunkNumber"
})
public class GetOutputChunk {

    @XmlElement(name = "sessionId", namespace = "")
    private String sessionId;
    @XmlElement(name = "chunkNumber", namespace = "")
    private int chunkNumber;

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

    /**
     * 
     * @return
     *     returns int
     */
    public int getChunkNumber() {
        return this.chunkNumber;
    }

    /**
     * 
     * @param chunkNumber
     *     the value for the chunkNumber property
     */
    public void setChunkNumber(int chunkNumber) {
        this.chunkNumber = chunkNumber;
    }

}
