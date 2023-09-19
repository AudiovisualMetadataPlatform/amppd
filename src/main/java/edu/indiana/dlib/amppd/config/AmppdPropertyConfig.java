package edu.indiana.dlib.amppd.config;


import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;


/**
 * Configuration for AMPPD related properties.
 */
@ConfigurationProperties(prefix="amppd")
@Validated
@Getter
@Setter
public class AmppdPropertyConfig {

    @NotNull private String environment;
    @NotNull private String dataRoot;
    @NotNull private String fileStorageRoot;
    @NotNull private String dropboxRoot;
    @NotNull private String symlinkDir;
    @NotNull private String mgmEvaluationResultsRoot;
    @NotNull private String mgmEvaluationScriptsRoot;
    @NotNull private String pythonPath;
    @NotNull private String mediaprobeDir;
    @NotNull private String encryptionSecret;  
    @NotNull private String adminEmail;
    @NotNull private String username; 
    @NotNull private String password; 
    @NotNull private String url;
    @NotNull private String corsOriginPattern;
    @NotNull private Boolean auth;
    @NotNull private String jwtSecret;
//    @NotNull private String workflowEditSecret;
    @NotNull private Integer jwtExpireMinutes;
    @NotNull private Integer workflowEditMinutes;
    @NotNull private Integer resetPasswordMinutes;
    @NotNull private Integer activateAccountDays;
//    @NotNull private int refreshResultsStatusMinutes;
    @NotNull private Integer refreshResultsTableMinutes;
    @NotNull private String refreshResultsStatusCron;
    @NotNull private String refreshResultsTableCron;
    @NotNull private Boolean refreshPermissionTables;
    @NotNull private Boolean refreshMgmTables;    
    @NotNull private Boolean refresUnitTable; 
    @NotNull private List<String> supplementCategories;
    @NotNull private List<String> groundtruthSubcategories;
    @NotNull private List<String> externalSources;
    @NotNull private List<String> taskManagers;
    @NotNull private List<String> unitRoles;
    @NotNull private Integer roleAssignmentMaxLevel;
    
}
