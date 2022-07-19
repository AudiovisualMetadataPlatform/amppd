package edu.indiana.dlib.amppd.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

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
		@Index(columnList = "name"),
		@Index(columnList = "category")
})
@Data
public class MgmScoringTool {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
       
    // must be uqniue within its parent category
	@NotNull
    private String name;
        
	@NotNull
    private String description;
        
	// current version
	@NotNull
    private String version;	
    
	// date when the current version is installed 
    @NotNull
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS")
    private Date upgradeDate;

    // output data type of the workflow result to be scored by the MST
    @NotNull
    private String workflowResultType; 
    
    // format (extentions) of the groundtruth file used by the MST
	@NotNull
    private String groundtruthFormat; 
    
	// static info of the parameters
	@OneToMany(mappedBy="mst", cascade = CascadeType.REMOVE)
	@JsonBackReference(value="parameters")
    private MgmScoringParameter parameters; 
    
	// path of the executable script of the MST, relative to the script root directory
	@NotNull
    private String scriptPath; 
    
	// category of the applicable MGMs for evaluation, corresponding to the Galaxy tool section
	@NotNull
	@ManyToOne
    private MgmCategory category;     
        
}
