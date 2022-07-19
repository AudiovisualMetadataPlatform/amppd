package edu.indiana.dlib.amppd.model;

import java.util.Set;

import javax.jdo.annotations.Unique;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Data;

/**
 * This class contains information of the category an MGM belongs to, corresponding to a section in Galaxy tool panel.
 * Note that the description part of this table is manually maintained, but the section info should be populated from Galaxy Tool API calls.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = {
		@Index(columnList = "name", unique = true),
		@Index(columnList = "sectionId")
})
@Data
public class MgmCategory {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
        
    // name of the category is the same as the name of the corresponding Galaxy section; must be unique
    @NotBlank
	@Unique
    private String name;	
    
	// ID of the corresponding Galaxy section; must be unique
    @NotBlank
	@Unique
    private String sectionId;	
    
	// Galaxy section doesn't include description property, so we need to add this on AMP side
    @Type(type="text")
    private String description; 
    
	@OneToMany(mappedBy="category", cascade = CascadeType.REMOVE)
	@JsonBackReference(value="msts")
    private Set<MgmScoringTool> msts;
	
}
