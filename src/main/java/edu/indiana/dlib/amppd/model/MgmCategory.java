package edu.indiana.dlib.amppd.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class contains information of the category an MGM belongs to, corresponding to a section in Galaxy tool panel.
 * Note that the table for this class is manually maintained, 
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class MgmCategory {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
        
	//@NotNull
    private String sectionId;	// ID of the corresponding Galaxy section
    
	//@NotNull
    private String sectionName;	// name of the corresponding Galaxy section
    
	//@NotNull
    private String description; // Galaxy section doesn't include descripton property, so we need to add this on AMP side
    
	@OneToMany(mappedBy="category", cascade = CascadeType.REMOVE)
	@JsonBackReference(value="msts")
    private Set<MgmScoringTool> msts;
	
}
