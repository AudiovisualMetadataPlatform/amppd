package edu.indiana.dlib.amppd.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * Primaryfile is a file containing actual media content of any MIME type. A primaryfile always associates with one and only one item.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class Primaryfile extends Asset {

	@OneToMany(mappedBy="primaryfile")
    private List<SupplementOfPrimaryfile> supplements;

	//  private Long itemId;
	@ManyToOne
	private Item item;
	
}
