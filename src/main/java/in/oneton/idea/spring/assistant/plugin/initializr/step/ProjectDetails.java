package in.oneton.idea.spring.assistant.plugin.initializr.step;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdkVersion;
import com.intellij.ui.CollectionComboBoxModel;
import in.oneton.idea.spring.assistant.plugin.initializr.InitializrModuleBuilder;
import in.oneton.idea.spring.assistant.plugin.initializr.ProjectCreationRequest;
import in.oneton.idea.spring.assistant.plugin.initializr.metadata.InitializerMetadata;
import in.oneton.idea.spring.assistant.plugin.initializr.metadata.InitializerMetadata.IdAndName;
import in.oneton.idea.spring.assistant.plugin.initializr.metadata.InitializerMetadata.IdAndNameComposite;
import in.oneton.idea.spring.assistant.plugin.initializr.metadata.InitializerMetadata.ProjectTypeComposite;
import in.oneton.idea.spring.assistant.plugin.initializr.metadata.InitializerMetadata.ProjectTypeComposite.ProjectType;

import javax.swing.*;
import java.util.List;

import static in.oneton.idea.spring.assistant.plugin.initializr.misc.InitializrUtil.from;
import static in.oneton.idea.spring.assistant.plugin.initializr.misc.InitializrUtil.newCollectionComboBoxModel;
import static in.oneton.idea.spring.assistant.plugin.misc.PsiCustomUtil.findFileUnderRootInModule;
import static java.util.Objects.requireNonNull;

public class ProjectDetails {

  private ProjectCreationRequest request;

  private JPanel rootPanel;
  private JTextField groupId;
  private JTextField artifactId;
  private JTextField projectName;
  private JTextField projectDescription;
  private JTextField packageName;
  private JComboBox<IdAndName> packagingType;
  private JComboBox<IdAndName> javaVersion;
  private JComboBox<ProjectType> projectType;
  private JComboBox<IdAndName> projectLanguage;
  private JTextField projectVersion;

  private JLabel groupIdLabel;
  private JLabel artifactIdLabel;
  private JLabel projectNameLabel;
  private JLabel projectDescriptionLabel;
  private JLabel packageNameLabel;
  private JLabel packagingTypeLabel;
  private JLabel javaVersionLabel;
  private JLabel projectTypeLabel;
  private JLabel projectLanguageLabel;
  private JLabel projectVersionLabel;

  public void init(InitializrModuleBuilder moduleBuilder, WizardContext wizardContext) {
    this.request = moduleBuilder.safeGetProjectCreationRequest();
    InitializerMetadata metadata = request.getMetadata();

    String prevGroupId = request.getSetProperty(request::setGroupId, request::getGroupId,
        metadata.getGroupIdHolder().getDefaultValue());
    groupId.setText(prevGroupId);
    groupId.addCaretListener(e -> request.setGroupId(groupId.getText()));

    String prevArtifactId = request.getSetProperty(request::setArtifactId, request::getArtifactId,
        metadata.getArtifactIdHolder().getDefaultValue());
    artifactId.setText(prevArtifactId);
    artifactId.addCaretListener(e -> request.setArtifactId(artifactId.getText()));

    String prevProjectName = request.getSetProperty(request::setName, request::getName,
        metadata.getNameHolder().getDefaultValue());
    projectName.setText(prevProjectName);
    projectName.addCaretListener(e -> request.setName(projectName.getText()));

    String prevProjectDescription = request
        .getSetProperty(request::setDescription, request::getDescription,
            metadata.getDescriptionHolder().getDefaultValue());
    projectDescription.setText(prevProjectDescription);
    projectDescription.addCaretListener(e -> request.setDescription(projectDescription.getText()));

    String prevPackageName = request
        .getSetProperty(request::setPackageName, request::getPackageName,
            metadata.getPackageNameHolder().getDefaultValue());
    packageName.setText(prevPackageName);
    packageName.addCaretListener(e -> request.setPackageName(packageName.getText()));

    String prevVersion = request.getSetProperty(request::setVersion, request::getVersion,
        metadata.getVersionHolder().getDefaultValue());
    projectVersion.setText(prevVersion);
    projectVersion.addCaretListener(e -> request.setVersion(projectVersion.getText()));

    IdAndNameComposite packagingTypeComposite = metadata.getPackagingTypeComposite();
    List<IdAndName> packagingTypes = packagingTypeComposite.getValues();
    IdAndName defaultPackagingType = request
        .getSetIdContainer(request::setPackaging, request::getPackaging, packagingTypes,
            packagingTypeComposite.getDefaultValue());
    String defaultPackagingTypeId =
        defaultPackagingType != null ? defaultPackagingType.getId() : null;
    CollectionComboBoxModel<IdAndName> packagingTypeComboBoxModel =
        newCollectionComboBoxModel(packagingTypes, defaultPackagingTypeId);
    packagingType.setModel(packagingTypeComboBoxModel);
    packagingType
        .addActionListener(e -> request.setPackaging((IdAndName) packagingType.getSelectedItem()));

    // TODO: Auto detect project java version if user is adding a module instead of new project
    IdAndNameComposite javaVersionComposite = metadata.getJavaVersionComposite();
    List<IdAndName> javaVersions = javaVersionComposite.getValues();
    IdAndName defaultJavaVersion = request
        .getSetIdContainer(request::setJavaVersion, request::getJavaVersion, javaVersions,
            javaVersionComposite.getDefaultValue());
    String defaultJavaVersionId = defaultJavaVersion != null ? defaultJavaVersion.getId() : null;
    CollectionComboBoxModel<IdAndName> javaVersionComboBoxModel =
        newCollectionComboBoxModel(javaVersions, defaultJavaVersionId);
    javaVersion.setModel(javaVersionComboBoxModel);
    javaVersion
        .addActionListener(e -> request.setJavaVersion((IdAndName) javaVersion.getSelectedItem()));

    IdAndNameComposite languageComposite = metadata.getLanguageComposite();
    List<IdAndName> languages = languageComposite.getValues();
    IdAndName defaultLanguage = request
        .getSetIdContainer(request::setLanguage, request::getLanguage, languages,
            languageComposite.getDefaultValue());
    String defaultLanguageId = defaultLanguage != null ? defaultLanguage.getId() : null;
    CollectionComboBoxModel<IdAndName> projectLanguageComboBoxModel =
        newCollectionComboBoxModel(languages, defaultLanguageId);

    projectLanguage.setModel(projectLanguageComboBoxModel);
    projectLanguage
        .addActionListener(e -> request.setLanguage((IdAndName) projectLanguage.getSelectedItem()));

    ProjectTypeComposite projectTypeComposite = metadata.getProjectTypeComposite();
    List<ProjectType> projectTypes = projectTypeComposite.getTypes();
    String defaultProjectTypeId = null;
    Project project = wizardContext.getProject();
    // TODO: Find a better way
    // for the cases where the user is trying to add module into an existing project
    if (project != null && project.isInitialized()) {
      boolean rootProjectIsGradle =
          findFileUnderRootInModule(wizardContext.getProject().getBaseDir(), "build.gradle")
              != null;
      if (rootProjectIsGradle) {
        defaultProjectTypeId = "gradle-project";
      } else {
        boolean rootProjectIsMaven =
            findFileUnderRootInModule(wizardContext.getProject().getBaseDir(), "pom.xml") != null;
        if (rootProjectIsMaven) {
          defaultProjectTypeId = "maven-project";
        }
      }
    }
    if (defaultProjectTypeId == null) {
      defaultProjectTypeId = projectTypeComposite.getDefaultValue();
    }
    ProjectType defaultProjectType = request
        .getSetIdContainer(request::setType, request::getType, projectTypes, defaultProjectTypeId);
    defaultProjectTypeId = defaultProjectType != null ? defaultProjectType.getId() : null;
    CollectionComboBoxModel<ProjectType> projectTypeComboBoxModel =
        newCollectionComboBoxModel(projectTypes, defaultProjectTypeId);
    projectType.setModel(projectTypeComboBoxModel);
    projectType
        .addActionListener(e -> request.setType((ProjectType) projectType.getSelectedItem()));
  }

  public JPanel getRoot() {
    return rootPanel;
  }

  public boolean validate(ModuleBuilder moduleBuilder, WizardContext wizardContext)
      throws ConfigurationException {
    if (!request.hasValidGroupId()) {
      throw new ConfigurationException("Invalid group id", "Invalid Data");
    } else if (!request.hasValidArtifactId()) {
      throw new ConfigurationException("Invalid artifact id", "Invalid Data");
    } else if (!request.hasValidVersion()) {
      throw new ConfigurationException("Invalid version", "Invalid Data");
    } else if (!request.hasValidName()) {
      throw new ConfigurationException("Invalid name", "Invalid Data");
    } else if (!request.hasValidPackageName()) {
      throw new ConfigurationException("Invalid package", "Invalid Data");
    } else if (!request.hasCompatibleJavaVersion(moduleBuilder, wizardContext)) {
      JavaSdkVersion wizardSdkVersion = from(wizardContext, moduleBuilder);
      throw new ConfigurationException("Selected Java version " + requireNonNull(
          IdAndName.class.cast(javaVersion.getSelectedItem())).getName()
          + " is not supported. Max supported version is (" + requireNonNull(wizardSdkVersion)
          .getMaxLanguageLevel().getCompilerComplianceDefaultOption()
          + ").\n\n You can go back to first screen and change the Project/Module SDK version there if you need support for newer Java versions",
          "Java Compatibility");
    }
    return true;
  }

}
