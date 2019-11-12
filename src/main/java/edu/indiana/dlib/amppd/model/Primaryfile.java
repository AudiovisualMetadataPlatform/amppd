package edu.indiana.dlib.amppd.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;

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
@Data
@EqualsAndHashCode(callSuper=true, onlyExplicitlyIncluded=true)
@ToString(callSuper=true, onlyExplicitlyIncluded=true)
// Lombok's impl of toString, equals, and hashCode doesn't handle circular references as in Bundle and Item and will cause StackOverflow exception.
public class Primaryfile extends Asset {

	/* Note:
	 * - The below history is not necessarily the same as where the primaryfile itself is uploaded in Galaxy
	 *   (for ex, currently, all assets including primaryfiles are loaded into the shared AMPPD history).
	 * - Each primaryfile has its own output history for all its associated AMP jobs. 
	 * 	 Supplements, on the other hand, don't associate with any output history on its own, as they only participate in a job run along with 
	 *   their associated primaryfile. So the historyId is only needed for primaryfile, not for supplements.
	 */	
	// ID of the history where all output datasets of all AMP jobs running against this primaryfile is stored in Galaxy.
    private String historyId;			

	@OneToMany(mappedBy="primaryfile")
    private Set<PrimaryfileSupplement> supplements;

	@ManyToOne
	private Item item;
		
    @ManyToMany
    @JsonBackReference
    private Set<Bundle> bundles;      
	
    @OneToMany(mappedBy="primaryfile")
    private Set<Job> jobs;        
    	    
//    @OneToMany(mappedBy="primaryfile")
//    private Set<Bag> bags;        

}
