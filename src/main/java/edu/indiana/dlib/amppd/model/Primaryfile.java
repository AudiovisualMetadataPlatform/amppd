package edu.indiana.dlib.amppd.model;

import java.util.Set;

import javax.jdo.annotations.Index;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;

import edu.indiana.dlib.amppd.validator.UniqueName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Primaryfile is a file containing actual media content of any MIME type. A primaryfile always associates with one and only one item.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = {@UniqueConstraint(name = "UniquePrimaryfileNamePerItem", columnNames = {"item_id", "name"})})
@UniqueName(message="primaryfile name must be unique within its parent item")
//@UniqueName(message="primaryfile name must be unique within its parent item", groups = {WithReference.class})
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class Primaryfile extends Asset {
	// Lombok's impl of toString, equals, and hashCode doesn't handle circular references as in Bundle and Item and will cause StackOverflow exception.

	/* Note:
	 * - The below history is not necessarily the same as where the primaryfile itself is uploaded in Galaxy
	 *   (for ex, currently, all assets including primaryfiles are loaded into the shared AMPPD history).
	 * - Each primaryfile has its own output history for all its associated AMP jobs. 
	 * 	 Supplements, on the other hand, don't associate with any output history on its own, as they only participate in a job run along with 
	 *   their associated primaryfile. So the historyId is only needed for primaryfile, not for supplements.
	 */	
	// ID of the history where all output datasets of all AMP jobs running against this primaryfile is stored in Galaxy.
	@Index(unique="true")	// historyId could be null, but it should be unique among all primaryfiles
    private String historyId;			

    // Note:
	// datasetId is moved from Asset to Primaryfile, as Supplement always input 
	// via Supplement MGM, no need to load into Galaxy library, thus no need for datasetId.
	private String datasetId;			// ID of the dataset as a result of upload to Galaxy

	@OneToMany(mappedBy="primaryfile", cascade = CascadeType.REMOVE)
	@JsonBackReference(value="supplements")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private Set<PrimaryfileSupplement> supplements;

//	@NotNull(groups = {WithReference.class})
	@NotNull
	@Index
	@ManyToOne
	private Item item;
		
    @ManyToMany(mappedBy = "primaryfiles")
	@JsonBackReference(value="bundles")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
    private Set<Bundle> bundles;  
    
    public Long getUnitId() {
    	return item.getUnitId();
    }    

    public Long getCollectionId() {
    	return item.getCollectionId();
    }
    
    public Long getItemId() {
    	return item.getId();
    }
    
    
//    @OneToMany(mappedBy="primaryfile")
//    private Set<Job> jobs;        
    	    
//    @OneToMany(mappedBy="primaryfile")
//    private Set<InputBag> bags;        
    
}
