
package monitorservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the monitorservice package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _CloseResponse_QNAME = new QName("http://MonitorService", "closeResponse");
    private final static QName _ExecuteCommandResponse_QNAME = new QName("http://MonitorService", "executeCommandResponse");
    private final static QName _LoadApplicationByFileNameResponse_QNAME = new QName("http://MonitorService", "loadApplicationByFileNameResponse");
    private final static QName _ExecuteAction_QNAME = new QName("http://MonitorService", "executeAction");
    private final static QName _ExecuteCommand_QNAME = new QName("http://MonitorService", "executeCommand");
    private final static QName _Logon_QNAME = new QName("http://MonitorService", "logon");
    private final static QName _GetOutputChunkResponse_QNAME = new QName("http://MonitorService", "getOutputChunkResponse");
    private final static QName _LoadApplicationByFileName_QNAME = new QName("http://MonitorService", "loadApplicationByFileName");
    private final static QName _AddServerResponse_QNAME = new QName("http://MonitorService", "addServerResponse");
    private final static QName _ExecuteActionResponse_QNAME = new QName("http://MonitorService", "executeActionResponse");
    private final static QName _RenameEnvironmentResponse_QNAME = new QName("http://MonitorService", "renameEnvironmentResponse");
    private final static QName _Close_QNAME = new QName("http://MonitorService", "close");
    private final static QName _AddApplicationResponse_QNAME = new QName("http://MonitorService", "addApplicationResponse");
    private final static QName _AddServer_QNAME = new QName("http://MonitorService", "addServer");
    private final static QName _RestartOutputsResponse_QNAME = new QName("http://MonitorService", "restartOutputsResponse");
    private final static QName _GetEnvironmentNames_QNAME = new QName("http://MonitorService", "getEnvironmentNames");
    private final static QName _SaveEnvironmentResponse_QNAME = new QName("http://MonitorService", "saveEnvironmentResponse");
    private final static QName _GetEnvironmentNamesResponse_QNAME = new QName("http://MonitorService", "getEnvironmentNamesResponse");
    private final static QName _KillRunningCommand_QNAME = new QName("http://MonitorService", "killRunningCommand");
    private final static QName _LogonResponse_QNAME = new QName("http://MonitorService", "logonResponse");
    private final static QName _KillRunningCommandResponse_QNAME = new QName("http://MonitorService", "killRunningCommandResponse");
    private final static QName _GetEnvironmentViewResponse_QNAME = new QName("http://MonitorService", "getEnvironmentViewResponse");
    private final static QName _DeleteEnvironmentResponse_QNAME = new QName("http://MonitorService", "deleteEnvironmentResponse");
    private final static QName _DeleteEnvironment_QNAME = new QName("http://MonitorService", "deleteEnvironment");
    private final static QName _GetOutputChunk_QNAME = new QName("http://MonitorService", "getOutputChunk");
    private final static QName _RenameEnvironment_QNAME = new QName("http://MonitorService", "renameEnvironment");
    private final static QName _RestartOutputs_QNAME = new QName("http://MonitorService", "restartOutputs");
    private final static QName _GetEnvironmentView_QNAME = new QName("http://MonitorService", "getEnvironmentView");
    private final static QName _AddApplication_QNAME = new QName("http://MonitorService", "addApplication");
    private final static QName _SaveEnvironment_QNAME = new QName("http://MonitorService", "saveEnvironment");
    private final static QName _DeleteRowResponse_QNAME = new QName("http://MonitorService", "deleteRowResponse");
    private final static QName _DeleteRow_QNAME = new QName("http://MonitorService", "deleteRow");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: monitorservice
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EnvironmentView }
     * 
     */
    public EnvironmentView createEnvironmentView() {
        return new EnvironmentView();
    }

    /**
     * Create an instance of {@link DeleteEnvironmentResponse }
     * 
     */
    public DeleteEnvironmentResponse createDeleteEnvironmentResponse() {
        return new DeleteEnvironmentResponse();
    }

    /**
     * Create an instance of {@link KillRunningCommand }
     * 
     */
    public KillRunningCommand createKillRunningCommand() {
        return new KillRunningCommand();
    }

    /**
     * Create an instance of {@link GetEnvironmentViewResponse }
     * 
     */
    public GetEnvironmentViewResponse createGetEnvironmentViewResponse() {
        return new GetEnvironmentViewResponse();
    }

    /**
     * Create an instance of {@link CloseResponse }
     * 
     */
    public CloseResponse createCloseResponse() {
        return new CloseResponse();
    }

    /**
     * Create an instance of {@link OutputInfo }
     * 
     */
    public OutputInfo createOutputInfo() {
        return new OutputInfo();
    }

    /**
     * Create an instance of {@link GetOutputChunk }
     * 
     */
    public GetOutputChunk createGetOutputChunk() {
        return new GetOutputChunk();
    }

    /**
     * Create an instance of {@link GetEnvironmentNamesResponse }
     * 
     */
    public GetEnvironmentNamesResponse createGetEnvironmentNamesResponse() {
        return new GetEnvironmentNamesResponse();
    }

    /**
     * Create an instance of {@link Application }
     * 
     */
    public Application createApplication() {
        return new Application();
    }

    /**
     * Create an instance of {@link Logon }
     * 
     */
    public Logon createLogon() {
        return new Logon();
    }

    /**
     * Create an instance of {@link DeleteRow }
     * 
     */
    public DeleteRow createDeleteRow() {
        return new DeleteRow();
    }

    /**
     * Create an instance of {@link RenameEnvironment }
     * 
     */
    public RenameEnvironment createRenameEnvironment() {
        return new RenameEnvironment();
    }

    /**
     * Create an instance of {@link GetOutputChunkResponse }
     * 
     */
    public GetOutputChunkResponse createGetOutputChunkResponse() {
        return new GetOutputChunkResponse();
    }

    /**
     * Create an instance of {@link EnvironmentView.Properties.Entry }
     * 
     */
    public EnvironmentView.Properties.Entry createEnvironmentViewPropertiesEntry() {
        return new EnvironmentView.Properties.Entry();
    }

    /**
     * Create an instance of {@link SaveEnvironment }
     * 
     */
    public SaveEnvironment createSaveEnvironment() {
        return new SaveEnvironment();
    }

    /**
     * Create an instance of {@link GetEnvironmentNames }
     * 
     */
    public GetEnvironmentNames createGetEnvironmentNames() {
        return new GetEnvironmentNames();
    }

    /**
     * Create an instance of {@link RenameEnvironmentResponse }
     * 
     */
    public RenameEnvironmentResponse createRenameEnvironmentResponse() {
        return new RenameEnvironmentResponse();
    }

    /**
     * Create an instance of {@link LoadApplicationByFileNameResponse }
     * 
     */
    public LoadApplicationByFileNameResponse createLoadApplicationByFileNameResponse() {
        return new LoadApplicationByFileNameResponse();
    }

    /**
     * Create an instance of {@link ExecuteCommand }
     * 
     */
    public ExecuteCommand createExecuteCommand() {
        return new ExecuteCommand();
    }

    /**
     * Create an instance of {@link AddServer }
     * 
     */
    public AddServer createAddServer() {
        return new AddServer();
    }

    /**
     * Create an instance of {@link OutputHistory }
     * 
     */
    public OutputHistory createOutputHistory() {
        return new OutputHistory();
    }

    /**
     * Create an instance of {@link LogonResult }
     * 
     */
    public LogonResult createLogonResult() {
        return new LogonResult();
    }

    /**
     * Create an instance of {@link AddApplicationResponse }
     * 
     */
    public AddApplicationResponse createAddApplicationResponse() {
        return new AddApplicationResponse();
    }

    /**
     * Create an instance of {@link DeleteEnvironment }
     * 
     */
    public DeleteEnvironment createDeleteEnvironment() {
        return new DeleteEnvironment();
    }

    /**
     * Create an instance of {@link RestartOutputsResponse }
     * 
     */
    public RestartOutputsResponse createRestartOutputsResponse() {
        return new RestartOutputsResponse();
    }

    /**
     * Create an instance of {@link DeleteRowResponse }
     * 
     */
    public DeleteRowResponse createDeleteRowResponse() {
        return new DeleteRowResponse();
    }

    /**
     * Create an instance of {@link EnvironmentView.Properties }
     * 
     */
    public EnvironmentView.Properties createEnvironmentViewProperties() {
        return new EnvironmentView.Properties();
    }

    /**
     * Create an instance of {@link RestartOutputs }
     * 
     */
    public RestartOutputs createRestartOutputs() {
        return new RestartOutputs();
    }

    /**
     * Create an instance of {@link LoadApplicationByFileName }
     * 
     */
    public LoadApplicationByFileName createLoadApplicationByFileName() {
        return new LoadApplicationByFileName();
    }

    /**
     * Create an instance of {@link GetEnvironmentView }
     * 
     */
    public GetEnvironmentView createGetEnvironmentView() {
        return new GetEnvironmentView();
    }

    /**
     * Create an instance of {@link EnvironmentViewRow }
     * 
     */
    public EnvironmentViewRow createEnvironmentViewRow() {
        return new EnvironmentViewRow();
    }

    /**
     * Create an instance of {@link Close }
     * 
     */
    public Close createClose() {
        return new Close();
    }

    /**
     * Create an instance of {@link KillRunningCommandResponse }
     * 
     */
    public KillRunningCommandResponse createKillRunningCommandResponse() {
        return new KillRunningCommandResponse();
    }

    /**
     * Create an instance of {@link AddServerResponse }
     * 
     */
    public AddServerResponse createAddServerResponse() {
        return new AddServerResponse();
    }

    /**
     * Create an instance of {@link AddApplication }
     * 
     */
    public AddApplication createAddApplication() {
        return new AddApplication();
    }

    /**
     * Create an instance of {@link CommandResult }
     * 
     */
    public CommandResult createCommandResult() {
        return new CommandResult();
    }

    /**
     * Create an instance of {@link ExecuteCommandResponse }
     * 
     */
    public ExecuteCommandResponse createExecuteCommandResponse() {
        return new ExecuteCommandResponse();
    }

    /**
     * Create an instance of {@link SaveEnvironmentResponse }
     * 
     */
    public SaveEnvironmentResponse createSaveEnvironmentResponse() {
        return new SaveEnvironmentResponse();
    }

    /**
     * Create an instance of {@link OutputChunkResult }
     * 
     */
    public OutputChunkResult createOutputChunkResult() {
        return new OutputChunkResult();
    }

    /**
     * Create an instance of {@link Action }
     * 
     */
    public Action createAction() {
        return new Action();
    }

    /**
     * Create an instance of {@link ExecuteActionResponse }
     * 
     */
    public ExecuteActionResponse createExecuteActionResponse() {
        return new ExecuteActionResponse();
    }

    /**
     * Create an instance of {@link ExecuteAction }
     * 
     */
    public ExecuteAction createExecuteAction() {
        return new ExecuteAction();
    }

    /**
     * Create an instance of {@link LogonResponse }
     * 
     */
    public LogonResponse createLogonResponse() {
        return new LogonResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CloseResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "closeResponse")
    public JAXBElement<CloseResponse> createCloseResponse(CloseResponse value) {
        return new JAXBElement<CloseResponse>(_CloseResponse_QNAME, CloseResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExecuteCommandResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "executeCommandResponse")
    public JAXBElement<ExecuteCommandResponse> createExecuteCommandResponse(ExecuteCommandResponse value) {
        return new JAXBElement<ExecuteCommandResponse>(_ExecuteCommandResponse_QNAME, ExecuteCommandResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoadApplicationByFileNameResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "loadApplicationByFileNameResponse")
    public JAXBElement<LoadApplicationByFileNameResponse> createLoadApplicationByFileNameResponse(LoadApplicationByFileNameResponse value) {
        return new JAXBElement<LoadApplicationByFileNameResponse>(_LoadApplicationByFileNameResponse_QNAME, LoadApplicationByFileNameResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExecuteAction }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "executeAction")
    public JAXBElement<ExecuteAction> createExecuteAction(ExecuteAction value) {
        return new JAXBElement<ExecuteAction>(_ExecuteAction_QNAME, ExecuteAction.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExecuteCommand }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "executeCommand")
    public JAXBElement<ExecuteCommand> createExecuteCommand(ExecuteCommand value) {
        return new JAXBElement<ExecuteCommand>(_ExecuteCommand_QNAME, ExecuteCommand.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Logon }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "logon")
    public JAXBElement<Logon> createLogon(Logon value) {
        return new JAXBElement<Logon>(_Logon_QNAME, Logon.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetOutputChunkResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "getOutputChunkResponse")
    public JAXBElement<GetOutputChunkResponse> createGetOutputChunkResponse(GetOutputChunkResponse value) {
        return new JAXBElement<GetOutputChunkResponse>(_GetOutputChunkResponse_QNAME, GetOutputChunkResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoadApplicationByFileName }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "loadApplicationByFileName")
    public JAXBElement<LoadApplicationByFileName> createLoadApplicationByFileName(LoadApplicationByFileName value) {
        return new JAXBElement<LoadApplicationByFileName>(_LoadApplicationByFileName_QNAME, LoadApplicationByFileName.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddServerResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "addServerResponse")
    public JAXBElement<AddServerResponse> createAddServerResponse(AddServerResponse value) {
        return new JAXBElement<AddServerResponse>(_AddServerResponse_QNAME, AddServerResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExecuteActionResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "executeActionResponse")
    public JAXBElement<ExecuteActionResponse> createExecuteActionResponse(ExecuteActionResponse value) {
        return new JAXBElement<ExecuteActionResponse>(_ExecuteActionResponse_QNAME, ExecuteActionResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RenameEnvironmentResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "renameEnvironmentResponse")
    public JAXBElement<RenameEnvironmentResponse> createRenameEnvironmentResponse(RenameEnvironmentResponse value) {
        return new JAXBElement<RenameEnvironmentResponse>(_RenameEnvironmentResponse_QNAME, RenameEnvironmentResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Close }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "close")
    public JAXBElement<Close> createClose(Close value) {
        return new JAXBElement<Close>(_Close_QNAME, Close.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddApplicationResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "addApplicationResponse")
    public JAXBElement<AddApplicationResponse> createAddApplicationResponse(AddApplicationResponse value) {
        return new JAXBElement<AddApplicationResponse>(_AddApplicationResponse_QNAME, AddApplicationResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddServer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "addServer")
    public JAXBElement<AddServer> createAddServer(AddServer value) {
        return new JAXBElement<AddServer>(_AddServer_QNAME, AddServer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestartOutputsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "restartOutputsResponse")
    public JAXBElement<RestartOutputsResponse> createRestartOutputsResponse(RestartOutputsResponse value) {
        return new JAXBElement<RestartOutputsResponse>(_RestartOutputsResponse_QNAME, RestartOutputsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetEnvironmentNames }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "getEnvironmentNames")
    public JAXBElement<GetEnvironmentNames> createGetEnvironmentNames(GetEnvironmentNames value) {
        return new JAXBElement<GetEnvironmentNames>(_GetEnvironmentNames_QNAME, GetEnvironmentNames.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SaveEnvironmentResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "saveEnvironmentResponse")
    public JAXBElement<SaveEnvironmentResponse> createSaveEnvironmentResponse(SaveEnvironmentResponse value) {
        return new JAXBElement<SaveEnvironmentResponse>(_SaveEnvironmentResponse_QNAME, SaveEnvironmentResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetEnvironmentNamesResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "getEnvironmentNamesResponse")
    public JAXBElement<GetEnvironmentNamesResponse> createGetEnvironmentNamesResponse(GetEnvironmentNamesResponse value) {
        return new JAXBElement<GetEnvironmentNamesResponse>(_GetEnvironmentNamesResponse_QNAME, GetEnvironmentNamesResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link KillRunningCommand }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "killRunningCommand")
    public JAXBElement<KillRunningCommand> createKillRunningCommand(KillRunningCommand value) {
        return new JAXBElement<KillRunningCommand>(_KillRunningCommand_QNAME, KillRunningCommand.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LogonResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "logonResponse")
    public JAXBElement<LogonResponse> createLogonResponse(LogonResponse value) {
        return new JAXBElement<LogonResponse>(_LogonResponse_QNAME, LogonResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link KillRunningCommandResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "killRunningCommandResponse")
    public JAXBElement<KillRunningCommandResponse> createKillRunningCommandResponse(KillRunningCommandResponse value) {
        return new JAXBElement<KillRunningCommandResponse>(_KillRunningCommandResponse_QNAME, KillRunningCommandResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetEnvironmentViewResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "getEnvironmentViewResponse")
    public JAXBElement<GetEnvironmentViewResponse> createGetEnvironmentViewResponse(GetEnvironmentViewResponse value) {
        return new JAXBElement<GetEnvironmentViewResponse>(_GetEnvironmentViewResponse_QNAME, GetEnvironmentViewResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteEnvironmentResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "deleteEnvironmentResponse")
    public JAXBElement<DeleteEnvironmentResponse> createDeleteEnvironmentResponse(DeleteEnvironmentResponse value) {
        return new JAXBElement<DeleteEnvironmentResponse>(_DeleteEnvironmentResponse_QNAME, DeleteEnvironmentResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteEnvironment }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "deleteEnvironment")
    public JAXBElement<DeleteEnvironment> createDeleteEnvironment(DeleteEnvironment value) {
        return new JAXBElement<DeleteEnvironment>(_DeleteEnvironment_QNAME, DeleteEnvironment.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetOutputChunk }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "getOutputChunk")
    public JAXBElement<GetOutputChunk> createGetOutputChunk(GetOutputChunk value) {
        return new JAXBElement<GetOutputChunk>(_GetOutputChunk_QNAME, GetOutputChunk.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RenameEnvironment }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "renameEnvironment")
    public JAXBElement<RenameEnvironment> createRenameEnvironment(RenameEnvironment value) {
        return new JAXBElement<RenameEnvironment>(_RenameEnvironment_QNAME, RenameEnvironment.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RestartOutputs }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "restartOutputs")
    public JAXBElement<RestartOutputs> createRestartOutputs(RestartOutputs value) {
        return new JAXBElement<RestartOutputs>(_RestartOutputs_QNAME, RestartOutputs.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetEnvironmentView }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "getEnvironmentView")
    public JAXBElement<GetEnvironmentView> createGetEnvironmentView(GetEnvironmentView value) {
        return new JAXBElement<GetEnvironmentView>(_GetEnvironmentView_QNAME, GetEnvironmentView.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddApplication }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "addApplication")
    public JAXBElement<AddApplication> createAddApplication(AddApplication value) {
        return new JAXBElement<AddApplication>(_AddApplication_QNAME, AddApplication.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SaveEnvironment }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "saveEnvironment")
    public JAXBElement<SaveEnvironment> createSaveEnvironment(SaveEnvironment value) {
        return new JAXBElement<SaveEnvironment>(_SaveEnvironment_QNAME, SaveEnvironment.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteRowResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "deleteRowResponse")
    public JAXBElement<DeleteRowResponse> createDeleteRowResponse(DeleteRowResponse value) {
        return new JAXBElement<DeleteRowResponse>(_DeleteRowResponse_QNAME, DeleteRowResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteRow }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://MonitorService", name = "deleteRow")
    public JAXBElement<DeleteRow> createDeleteRow(DeleteRow value) {
        return new JAXBElement<DeleteRow>(_DeleteRow_QNAME, DeleteRow.class, null, value);
    }

}
