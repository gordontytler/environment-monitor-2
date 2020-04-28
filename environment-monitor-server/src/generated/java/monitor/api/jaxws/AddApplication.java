
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "addApplication", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addApplication", namespace = "http://MonitorService", propOrder = {
    "sessionId",
    "nameInEnvironmentView",
    "fileName",
    "outputName"
})
public class AddApplication {

    @XmlElement(name = "sessionId", namespace = "")
    private String sessionId;
    @XmlElement(name = "nameInEnvironmentView", namespace = "")
    private String nameInEnvironmentView;
    @XmlElement(name = "fileName", namespace = "")
    private String fileName;
    @XmlElement(name = "outputName", namespace = "")
    private String outputName;

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
     *     returns String
     */
    public String getNameInEnvironmentView() {
        return this.nameInEnvironmentView;
    }

    /**
     * 
     * @param nameInEnvironmentView
     *     the value for the nameInEnvironmentView property
     */
    public void setNameInEnvironmentView(String nameInEnvironmentView) {
        this.nameInEnvironmentView = nameInEnvironmentView;
    }

    /**
     * 
     * @return
     *     returns String
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * 
     * @param fileName
     *     the value for the fileName property
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 
     * @return
     *     returns String
     */
    public String getOutputName() {
        return this.outputName;
    }

    /**
     * 
     * @param outputName
     *     the value for the outputName property
     */
    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

}
