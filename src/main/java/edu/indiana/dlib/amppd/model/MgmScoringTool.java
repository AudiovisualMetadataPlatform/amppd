package edu.indiana.dlib.amppd.model;

import java.util.Set;

import javax.jdo.annotations.Unique;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
		@Index(columnList = "toolId"),
		@Index(columnList = "name"),
		@Index(columnList = "workflowResultType"),
		@Index(columnList = "groundtruthFormat"),
		@Index(columnList = "workflowResultType, groundtruthFormat"),
		@Index(columnList = "category_id")
})
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class MgmScoringTool extends AmpObject {
       
    // human-readable tool ID of the scoring tool, must be unique
    @NotBlank
	@Unique
    private String toolId;	
    
    // name of the scoring tool must be unique within its parent category
    @NotBlank
    private String name;
        
    @NotBlank
    @Type(type="text")
    private String description;
        
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
    
    // format (extensions) of the groundtruth file used by the MST
    @NotBlank
    private String groundtruthFormat; 
    
	// static info of the parameters
	@OneToMany(mappedBy="mst", cascade = CascadeType.REMOVE)
	@JsonBackReference(value="parameters")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
    private Set<MgmScoringParameter> parameters; 
    
	// category of the applicable MGMs for evaluation, corresponding to the Galaxy tool section
	@NotNull
	@ManyToOne
    private MgmCategory category;     
       
	// temporary storage of section ID of the MST for CSV parsing purpose
	@Transient
	private String sectionId;		

}
