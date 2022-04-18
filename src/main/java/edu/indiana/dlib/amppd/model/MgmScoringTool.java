package edu.indiana.dlib.amppd.model;

import java.util.Date;

import javax.jdo.annotations.Index;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

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
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Data
@NoArgsConstructor
public class MgmScoringTool {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
       
	//@NotNull
	@Index
    private String name;
        
	//@NotNull
    private String description;
        
	//@NotNull
    private String version;	
    
    //@NotNull
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS")
    private Date upgradeDate; // date when this version of the MST is installed 

    //@NotNull
	@Index
    private String workflowResultDataType; // Galaxy data type of the workflow result to be scored by the MST
    
	//@NotNull
	@Index
    private String groundTruthFormat; // format of the groundtruth file used by the MST
    
	//@NotNull
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String parameters; // JSON representation of the MST parameters map as <name, format> pairs
//  private Map<String, String> parameters; // <name, format> map of the parameters of the MST
    
	//@NotNull
    private String scriptPath; // path of the executable script of the MST, relative to the script root directory
    
	//@NotNull
	@Index
	@ManyToOne
    private MgmCategory category; // category of the associated MGM, corresponding to the tool panel section in Galaxy
    
	//@NotNull
	@Index
    private String mgmToolId;	// ID of the associated MGM in galaxy
        
}
