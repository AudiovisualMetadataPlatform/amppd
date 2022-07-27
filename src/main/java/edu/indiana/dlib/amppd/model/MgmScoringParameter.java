package edu.indiana.dlib.amppd.model;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * This class contains information about a parameter of an MGM Scoring Tool (MST).
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = {
		@Index(columnList = "name"),
		@Index(columnList = "mst_id")
})
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class MgmScoringParameter extends AmpObject {
	
	public enum ParamType {TEXT, SELECT, NUMBER};
       
    // must be unique within its parent mst
    @NotBlank
    private String name; 
        
    @NotBlank
    @Type(type="text")
    private String description;
	
    // value type
    @NotBlank
    private ParamType type;
	
    // value range for NUMBER parameter
    private Float min, max;
	    
    // value set selections for SELECT type of parameter, a comma separated list of words/phrases
    private String selections;

    // category of the associated MGM, corresponding to the tool panel section in Galaxy
	@NotNull
	@ManyToOne
    private MgmScoringTool mst; 

	// temporary storage of script of the parameter for CSV parsing purpose
	@Transient
	private String mstName;
	
}
