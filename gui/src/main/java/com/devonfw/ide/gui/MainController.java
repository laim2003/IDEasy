package com.devonfw.ide.gui;

import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devonfw.ide.gui.context.IdeGuiStateManager;
import com.devonfw.ide.gui.context.ProjectManager;
import com.devonfw.ide.gui.modal.IdeDialog;

/**
 * Controller of the main screen of the dashboard GUI.
 */
public class MainController {

  private static Logger LOG = LoggerFactory.getLogger(MainController.class);

  private ProjectManager projectManager;


  @FXML
  private ComboBox<String> selectedProject;

  @FXML
  private ComboBox<String> selectedWorkspace;

  @FXML
  private Button androidStudioOpen;

  @FXML
  private Button eclipseOpen;

  @FXML
  private Button intellijOpen;

  @FXML
  private Button vsCodeOpen;

  private final String directoryPath;

  /**
   * Constructor
   */
  public MainController(String directoryPath) {

    LOG.debug("IDE_ROOT path={}", directoryPath);
    this.directoryPath = directoryPath;

    this.projectManager = IdeGuiStateManager.getInstance().getProjectManager();
  }

  @FXML
  private void initialize() {

    setProjectsComboBox();
  }

  @FXML
  private void openAndroidStudio() {

    openIDE("android-studio");
  }

  @FXML
  private void openEclipse() {

    openIDE("eclipse");
  }

  @FXML
  private void openIntellij() {

    openIDE("intellij");
  }

  @FXML
  private void openVsCode() {

    openIDE("vscode");
  }

  private void setProjectsComboBox() {

    assert (directoryPath != null) : "directoryPath is null! Please check the setup of your environment variables (IDE_ROOT)";

    List<String> projects = projectManager.getProjectNames();

    selectedProject.getItems().clear();
    selectedProject.getItems().addAll(projects);

    selectedProject.setOnAction(_ -> {

      setWorkspaceComboBox();

      selectedWorkspace.setDisable(false);
    });
  }

  private void setWorkspaceComboBox() {

    List<String> workspaces = null;
    try {
      workspaces = projectManager.getWorkspaceNames(selectedProject.getValue());
    } catch (NotDirectoryException e) {
      throw new RuntimeException(e);
    }

    selectedWorkspace.getItems().clear();
    selectedWorkspace.getItems().addAll(workspaces);

    selectedWorkspace.setOnAction(actionEvent -> {
      updateContext(selectedProject.getValue(), selectedWorkspace.getValue());

      androidStudioOpen.setDisable(false);
      eclipseOpen.setDisable(false);
      intellijOpen.setDisable(false);
      vsCodeOpen.setDisable(false);
    });
  }

  private void openIDE(String inIde) {

    IdeGuiStateManager
        .getInstance()
        .getCurrentContext()
        .getCommandletManager()
        .getCommandlet(inIde)
        .run();
  }

  private void updateContext(String selectedProjectName, String selectedWorkspaceName) {
    try {
      //TODO: remove questions before PR
      IdeGuiStateManager guiStateManager = IdeGuiStateManager.getInstance();
      guiStateManager.switchContext(selectedProjectName, selectedWorkspaceName);
      guiStateManager.getCurrentContext().question("Test yes/no question");
      guiStateManager.getCurrentContext().askToContinue("Do you want to continue? with project {}?", selectedProjectName);
      guiStateManager.getCurrentContext().question(new String[] { "Green", "blue", "yellow", "red" }, "Do a selection:", selectedProjectName);
    } catch (FileNotFoundException e) {
      IdeDialog errorDialog = new IdeDialog(IdeDialog.AlertType.ERROR, e.getMessage());
      errorDialog.showAndWait();
    }
  }
}
