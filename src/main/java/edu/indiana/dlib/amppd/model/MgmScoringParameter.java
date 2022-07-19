package edu.indiana.dlib.amppd.model;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * This class contains information about a parameter of an MGM Scoring Tool (MST).
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = {
		@Index(columnList = "name"),
		@Index(columnList = "mst")
})
@Data
public class MgmScoringParameter {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
       
    // must be unique within its parent mst
	@NotNull
    private String name; 
        
	@NotNull
    private String description;
	
	// category of the associated MGM, corresponding to the tool panel section in Galaxy
	@NotNull
	@ManyToOne
    private MgmScoringTool mst; 

}
