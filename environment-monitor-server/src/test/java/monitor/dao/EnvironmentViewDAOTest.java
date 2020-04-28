package monitor.dao;


import monitor.model.CommandResult;
import monitor.model.Configuration;
import monitor.model.EnvironmentView;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class EnvironmentViewDAOTest {

    public static final String TEST_ENVIRONMENT_NAME = "Test environment";
    public static String THIS_TEST_COMPUTER;

    @Before
    public void before() {
        THIS_TEST_COMPUTER = Configuration.getInstance().getDefaultHostForTests();
        EnvironmentViewDAO.getInstance().resetCache();
    }

    @Test
    public void addApplication() {
        EnvironmentViewDAO dao = EnvironmentViewDAO.getInstance();
        dao.resetCache();
        EnvironmentView oldView = dao.loadEnvironmentView(TEST_ENVIRONMENT_NAME);
        int oldViewSize = oldView.getRows().size();
        System.out.println("before adding application\n\n" + oldView.getRows());

        // There are three outputs but only one is in the Section "Menu"
        CommandResult result = dao.addApplication(TEST_ENVIRONMENT_NAME, MockEnvironmentViewDAO.THIS_TEST_COMPUTER, "test added application", "applications/test-added-application-001.txt", null);
        Assert.assertEquals(
                "\nadded 'sh logger.sh' output for 'test added application' application on " + THIS_TEST_COMPUTER + " server.\n\n",
                result.getOutput());

        EnvironmentView newView = dao.loadEnvironmentView(TEST_ENVIRONMENT_NAME);
        System.out.println("\nafter adding application\n\n" + newView.getRows());
        // only 2 rows added because the row with no application and no output has been omitted
        // this one:  EnvironmentViewRow [application=Application [fileName=, nameInEnvironmentView=], outputName=, serverName=gordon-netbook],
        Assert.assertEquals(oldViewSize, newView.getRows().size());
    }

    @Test
    public void saveAndLoadEnvironmentView() {
        MockEnvironmentViewDAO mockDAO = new MockEnvironmentViewDAO();

        EnvironmentView environmentView = mockDAO.loadEnvironmentView(TEST_ENVIRONMENT_NAME);
        EnvironmentViewDAO fileDAO = EnvironmentViewDAO.getInstance();
        fileDAO.saveEnvironmentView(environmentView);

        EnvironmentView reloaded = fileDAO.loadEnvironmentView(TEST_ENVIRONMENT_NAME);

        Assert.assertEquals(environmentView.toString(), reloaded.toString());
        //Assert.assertEquals(environmentView, reloaded);
    }


    @Test
    public void addOutput() {
        EnvironmentViewDAO dao = EnvironmentViewDAO.getInstance();
        dao.resetCache();
        EnvironmentView oldView = dao.loadEnvironmentView(TEST_ENVIRONMENT_NAME);
        int oldViewSize = oldView.getRows().size();
        System.out.println("\nbefore adding output\n\n" + oldView.getRows());

        CommandResult result = dao.addApplication(TEST_ENVIRONMENT_NAME, MockEnvironmentViewDAO.THIS_TEST_COMPUTER, "test added application", "applications/test-added-application-001.txt", "python");
        Assert.assertEquals(
                "added 'python' output for 'test added application' application on " + THIS_TEST_COMPUTER + " server.",
                result.getOutput());

        EnvironmentView newView = dao.loadEnvironmentView(TEST_ENVIRONMENT_NAME);
        System.out.println("\nafter adding output\n\n" + newView.getRows());
        // we added one row but the row with no application and no output has now been omitted
        Assert.assertEquals(oldViewSize + 1, newView.getRows().size());
    }

}
