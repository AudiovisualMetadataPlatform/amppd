package edu.indiana.dlib.amppd.model;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.validation.constraints.NotBlank;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import edu.indiana.dlib.amppd.validator.EnumConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Supplement is a file (either media or annotation) used as supplemental material to assist metadata retrieval for a primaryfile through a workflow.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public abstract class Supplement extends Asset {

	@NotBlank
	@EnumConfig(property = "supplementCategories")	
	private String category;
	
	// In batch manifest the types are indicated as U or "Unit", C or "Collection", I or "Item", P or "Primaryfile"
	// Note: PFILE is not a Supplement type per se, but refers to Primaryfile as one special Asset type 
	public enum SupplementType { PFILE, UNIT, COLLECTION, ITEM, PRIMARYFILE }
	
	// TODO double check the relationship
//	@ManyToMany
//	private Set<InputBag> bags;
	
	/**
	 * Convert the given supplemental file type string to the corresponding SupplementType enum.
	 */
	public static SupplementType getSupplementType(String type) {
		switch(type.trim().toLowerCase()) {
			case "p":
			case "primary":
			case "primaryfile":
				return SupplementType.PRIMARYFILE;
			case "i":
			case "item":
				return SupplementType.ITEM;
			case "c":
			case "collection":
				return SupplementType.COLLECTION;
			case "u":
			case "unit":
				return SupplementType.UNIT;
			default:
				return null;
		}
	}	
	
	/**
	 * Copy all fields other than ID and parent of the given supplement to this one.
	 */
	public void copy(Supplement supplement) {
		// no need to copy ID as it will be ignored if this supplement is of a different type than the given one;
		// and the copy is only used in such cases.
		setCreatedBy(supplement.getCreatedBy());
		setModifiedBy(supplement.getModifiedBy());
		setCreatedDate(supplement.getCreatedDate());
		setModifiedDate(supplement.getModifiedDate());
		setName(supplement.getName());
		setDescription(supplement.getDescription());
		setOriginalFilename(supplement.getOriginalFilename());
		setPathname(supplement.getPathname());
		setSymlink(supplement.getSymlink());
		setMediaInfo(supplement.getMediaInfo());
		setCategory(supplement.getCategory());
	}
	
}
