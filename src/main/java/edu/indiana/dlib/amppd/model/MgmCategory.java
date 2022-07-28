package edu.indiana.dlib.amppd.model;

import java.util.Set;

import javax.jdo.annotations.Unique;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class MgmCategory extends AmpObject {
        
	// ID of the corresponding tool section, provided in tool config in Galaxy; must be unique
    @NotBlank
	@Unique
    private String sectionId;	

    // name of the category, populated from the name of the corresponding Galaxy section; must be unique
    @NotBlank
	@Unique
    private String name;	
        
	// Galaxy section doesn't include description property, so we need to add this on AMP side
    @NotBlank
    @Type(type="text")
    private String description; 
    
	@OneToMany(mappedBy="category", cascade = CascadeType.REMOVE)
	@JsonBackReference(value="mgms")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
    private Set<MgmTool> mgms;
	
	@OneToMany(mappedBy="category", cascade = CascadeType.REMOVE)
	@JsonBackReference(value="msts")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
    private Set<MgmScoringTool> msts;
	
}