
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "executeCommand", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "executeCommand", namespace = "http://MonitorService", propOrder = {
    "command",
    "sessionId"
})
public class ExecuteCommand {

    @XmlElement(name = "command", namespace = "")
    private String command;
    @XmlElement(name = "sessionId", namespace = "")
    private String sessionId;

    /**
     * 
     * @return
     *     returns String
     */
    public String getCommand() {
        return this.command;
    }

    /**
     * 
     * @param command
     *     the value for the command property
     */
    public void setCommand(String command) {
        this.command = command;
    }

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
