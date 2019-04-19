package edu.indiana.dlib.amppd.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.Data;

/**
 * Primary is a file containing actual media content of any MIME type. A primary always associates with one and only one item.
 * @author yingfeng
 *
 */
@Entity
@Data
public class Primary extends Asset {

	@OneToMany(mappedBy="primary")
    private List<SupplementOfPrimary> supplements;

	//  private Long itemId;
	@ManyToOne
	private Item item;
	
}
