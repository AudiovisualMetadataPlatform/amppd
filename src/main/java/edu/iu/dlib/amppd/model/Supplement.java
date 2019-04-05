package edu.iu.dlib.amppd.model;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import lombok.Data;

/**
 * Supplement is a file (either media or annotation) used as supplemental material to assist metadata retrieval for a primary through a workflow.
 * @author yingfeng
 *
 */
@Entity
@Data
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public class Supplement extends Asset {
	
}
