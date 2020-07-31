package edu.indiana.dlib.amppd.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;

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
@Data
@EqualsAndHashCode(callSuper=true, onlyExplicitlyIncluded=true)
@ToString(callSuper=true, onlyExplicitlyIncluded=true)
// Lombok's impl of toString, equals, and hashCode doesn't handle circular references as in Bundle and Item and will cause StackOverflow exception.
public class Bundle extends Dataentity {

//	TODO JsonManagedReference annotation was added to resolve a recursive reference issue due to M:M relationship with Item, but it causes exceptions in repository requests   
//	@JsonManagedReference	
//	@ManyToMany(mappedBy = "bundles")
	// let Bundle owns the many to many relationship, since conceptually bundle is the container of primaryfiles, 
	// and our use case is often updating bundle's primaryfiles instead of the other way around
	@ManyToMany
    @JoinTable(name = "bundle_primaryfile", joinColumns = @JoinColumn(name = "bundle_id"), inverseJoinColumns = @JoinColumn(name = "primaryfile_id"))
	@JsonBackReference(value="primaryfiles")
    private Set<Primaryfile> primaryfiles;

//	@ManyToMany(mappedBy = "bundles")
//    private Set<Item> items;
    
//    @ManyToMany(mappedBy = "bundles")
//    private Set<Bag> bags;

}
  

