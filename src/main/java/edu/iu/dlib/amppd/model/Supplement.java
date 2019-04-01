package edu.iu.dlib.amppd.model;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Supplement is a file (either media or annotation) used as supplemental material to assist metadata retrieval for a masterfile through a workflow.
 * @author yingfeng
 *
 */
@Entity
@Getter @Setter @NoArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE")
public class Supplement extends Asset {
	
}
