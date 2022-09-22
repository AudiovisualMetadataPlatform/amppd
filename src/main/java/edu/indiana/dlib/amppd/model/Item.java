package edu.indiana.dlib.amppd.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import edu.indiana.dlib.amppd.validator.UniqueExternalSrcAndId;
import edu.indiana.dlib.amppd.validator.UniqueName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.jdo.annotations.Index;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Item represents an intellectual object that contains one or more primaryfiles and none or multiple supplement files.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = {@UniqueConstraint(name = "UniqueItemNamePerCollection", columnNames = {"collection_id", "name"}),
		@UniqueConstraint(name = "UniqueExternalSrcAndIdPerCollection", columnNames = {"collection_id", "externalId", "externalSource"})})
@UniqueName(message="item name must be unique within its parent collection")
@UniqueExternalSrcAndId(message="External Source and ID must be unique within its parent collection")
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class Item extends Content {
    
	@OneToMany(mappedBy="item", cascade = CascadeType.REMOVE)
	@JsonBackReference(value="primaryfiles")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
    private Set<Primaryfile> primaryfiles;

	@OneToMany(mappedBy="item", cascade = CascadeType.REMOVE)
	@JsonBackReference(value="supplements")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
    private Set<ItemSupplement> supplements;

	@NotNull
	@Index
	@ManyToOne
	private Collection collection;	
    
//    @ManyToMany
//    @JsonBackReference
//    private Set<Bundle> bundles;      
    	
}
