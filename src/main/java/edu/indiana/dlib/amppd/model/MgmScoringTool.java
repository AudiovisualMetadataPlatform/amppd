package edu.indiana.dlib.amppd.model;

import java.util.Date;
import java.util.Map;

import javax.jdo.annotations.Index;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

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
@Data
@NoArgsConstructor
public class MgmScoringTool {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
       
	//@NotNull
    private String name;
        
	//@NotNull
    private String description;
        
	//@NotNull
	@Index
	@ManyToOne
    private Category category; // category of the associated MGM, corresponding to the tool panel section in Galaxy
    
	//@NotNull
	@Index
    private String mgmToolId;	// ID of the associated MGM in galaxy
        
	//@NotNull
    private String version;	
    
	//@NotNull
    private String workflowResultDataType; // Galaxy data type of the workflow result to be scored by the MST
    
	//@NotNull
    private String groundTruthFormat; // format of the groundtruth file used by the MST
    
	//@NotNull
    private Map<String, String> parameters; // <name, format> map of the parameters of the MST
    
	//@NotNull
    private String scriptPath; // path of the executable script of the MST, relative to the script root directory
    
    //@NotNull
	@Index
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS")
    private Date upgradeDate; // date when this version of the MST is installed 

}
