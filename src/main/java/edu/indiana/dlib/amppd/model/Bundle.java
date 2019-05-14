package edu.indiana.dlib.amppd.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToMany;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * Bundle is a container of one or multiple bags to which similar workflows can be applied.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class Bundle extends Dataentity {

	@ManyToMany(mappedBy = "bundles")
    private List<Item> items;
    
//    @ManyToMany(mappedBy = "bundles")
//    private List<Bag> bags;

}
  

