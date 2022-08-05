package edu.indiana.dlib.amppd.model;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
		@Index(columnList = "mst_id"),
		@Index(columnList = "mst_id, name", unique = true)
})
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class MgmScoringParameter extends AmpObject {
	
	public enum ParamType {TEXT, SINGLE_SELECT, MULTI_SELECT, INTEGER, FLOAT};
       
    // must be unique within its parent mst
    @NotBlank
    private String name; 
        
    @NotBlank
    @Type(type="text")
    private String description;
	
    // value type
    @NotBlank
	@Enumerated(EnumType.STRING)
    private ParamType type;
	
    // value range for INTEGER/FLOAT parameter
    private Double min, max;
	    
    // value set selections for SELECT type of parameter, a comma separated list of words/phrases
    @Type(type="text")
    private String selections;

    // the sibling parameter (of the same parent mst) on which this parameter's value set depends on, applicable to sing/multi-select ParamType
    @ManyToOne
    private MgmScoringParameter dependency;
    
    // category of the associated MGM, corresponding to the tool panel section in Galaxy
	@NotNull
	@ManyToOne
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private MgmScoringTool mst; 

	// temporary storage of name of the dependency parameter for CSV parsing purpose
	@Transient
	private String dependencyName;

	// temporary storage of toolId of the parent mst of the parameter for CSV parsing purpose
	@Transient
	private String mstToolId;
		
}
