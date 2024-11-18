package edu.indiana.dlib.amppd.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Formula;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.indiana.dlib.amppd.validator.UniqueName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Bundle is a container of primaryfiles to which similar workflows can be applied.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = {@UniqueConstraint(name = "UniqueBundleName", columnNames = {"name"})})
@UniqueName(message="bundle name must be unique")
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
// Lombok's impl of toString, equals, and hashCode doesn't handle circular references as in Bundle and Item and will cause StackOverflow exception.
public class Bundle extends Dataentity {

	// let Bundle owns the many to many relationship, since conceptually bundle is the container of primaryfiles, 
	// and our use case is often updating bundle's primaryfiles instead of the other way around
	@ManyToMany
    @JoinTable(name = "bundle_primaryfile", joinColumns = @JoinColumn(name = "bundle_id"), inverseJoinColumns = @JoinColumn(name = "primaryfile_id"))
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
//	@JsonManagedReference	
    private Set<Primaryfile> primaryfiles;

//	@ManyToMany(mappedBy = "bundles")
//    private Set<Item> items;
    
//    @ManyToMany(mappedBy = "bundles")
//    private Set<InputBag> bags;

	// TODO for now deleting bundle is not used or implemented
    @Formula("false")
    private Boolean deletable; 
    
	@JsonIgnore
    public Long getAcUnitId() {
    	return null;
    }

}
  

