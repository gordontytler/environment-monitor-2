
package monitor.api.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "loadApplicationByFileNameResponse", namespace = "http://MonitorService")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "loadApplicationByFileNameResponse", namespace = "http://MonitorService")
public class LoadApplicationByFileNameResponse {

    @XmlElement(name = "application", namespace = "")
    private monitor.model.Application application;

    /**
     * 
     * @return
     *     returns Application
     */
    public monitor.model.Application getApplication() {
        return this.application;
    }

    /**
     * 
     * @param application
     *     the value for the application property
     */
    public void setApplication(monitor.model.Application application) {
        this.application = application;
    }

}
