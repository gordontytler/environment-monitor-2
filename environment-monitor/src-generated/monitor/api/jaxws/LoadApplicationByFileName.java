
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "loadApplicationByFileName", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "loadApplicationByFileName", namespace = "http://MonitorService", propOrder = {
    "fileName",
    "nameInEnvironmentView"
})
public class LoadApplicationByFileName {

    @XmlElement(name = "fileName", namespace = "")
    private String fileName;
    @XmlElement(name = "nameInEnvironmentView", namespace = "")
    private String nameInEnvironmentView;

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

}
