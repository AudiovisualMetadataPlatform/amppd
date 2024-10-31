package edu.indiana.dlib.amppd.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import edu.indiana.dlib.amppd.service.impl.ConfigServiceImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.jdo.annotations.Unique;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * This class contains information about an MGM Scoring Tool (MST) associated with an executable script,
 * to compute evaluation scores of its corresponding MGM.
 * Note that the table for this class is manually maintained, i.e. each time a new MST (or a new version of it) 
 * is installed, this table shall be updated with that information. 
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = {
		@Index(columnList = "toolId", unique = true),
		@Index(columnList = "name"),
		@Index(columnList = "workflowResultType"),
		@Index(columnList = "groundtruthSubcategory"),
		@Index(columnList = "groundtruthFormat"),
		@Index(columnList = "workflowResultType, groundtruthSubcategory, groundtruthFormat"),
		@Index(columnList = "category_id"),
		@Index(columnList = "category_id, name", unique = true)
})
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class MgmScoringTool extends MgmMeta {
       
    // human-readable tool ID of the scoring tool, must be unique
    @NotBlank
	@Unique
    private String toolId;	
    
    // name of the scoring tool must be unique within its parent category
//    @NotBlank
//    private String name;
        
//    @NotBlank
//    @Type(type="text")
//    private String description;
        
    // TODO create separate data model class/table for MST versions
    
//	// current version
//    @NotBlank
//    private String version;	
//    
//	// date when the current version is installed 
//    @NotNull
//    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS")
//    private Date upgradeDate;

	// path of the executable script of the MST, relative to the script root directory
	@NotBlank
    private String scriptPath; 
    
    // output data type of the workflow result to be scored by the MST
    @NotBlank
    private String workflowResultType; 
    
    // subcategory of the groundtruth supplement used by the MST
    // Note: this field is a simple string for most tools, but could also be a JSON string of a map, 
    // between the depending parameter value and the actual subcategory: { paramName: subcategory }
    @NotBlank
    private String groundtruthSubcategory; 
    
    // format (extensions) of the groundtruth file used by the MST
    @NotBlank
    private String groundtruthFormat; 

    // static info of the parameters
	@OneToMany(mappedBy="mst", cascade = CascadeType.REMOVE, orphanRemoval = true)
	@JsonBackReference(value="parameters")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
    private Set<MgmScoringParameter> parameters; 
    
	// category of the applicable MGMs for evaluation, corresponding to the Galaxy tool section
	@NotNull
	@ManyToOne
    private MgmCategory category;     
       
    // the parameter on which the groundtruthSubcategory depends on, applicable to sing/multi-select ParamType;
	// when null, the groundtruthSubcategory is a simple string;
	// when not null, the groundtruthSubcategory is a map JSON.
	// Note that the parameter must be one of those associated with this scoring tool    
    @OneToOne
//    @OneToOne(mappedBy = "mst", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private MgmScoringParameter dependencyParameter;

	// temporary storage of name of the dependency parameter for CSV parsing purpose
	@Transient
	private String dependencyParamName;
	
	// temporary storage of section ID of the MST for CSV parsing purpose
	@Transient
	private String sectionId;

	@NotBlank
	private String groundtruthTemplate;

	private String useCase;
	private String workflowResultOutput;

	/**
	 * Get the concatenated groundtruth category in the form of Groundtruth-subcategory
	 */
	public String getGroundtruthCategory() {
		return ConfigServiceImpl.getGroundtruthCategory(groundtruthSubcategory);
	}
	
}
