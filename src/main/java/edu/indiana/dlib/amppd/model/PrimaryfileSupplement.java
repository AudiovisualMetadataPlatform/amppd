package edu.indiana.dlib.amppd.model;

import javax.jdo.annotations.Index;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import edu.indiana.dlib.amppd.validator.UniqueName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * PrimaryfileSupplement is a supplemental file associated with a primaryfile and only available for this primaryfile.
 * @author yingfeng
 *
 */
@Data
@EqualsAndHashCode(callSuper=true, onlyExplicitlyIncluded=true)
@ToString(callSuper=true, onlyExplicitlyIncluded=true)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = {@UniqueConstraint(name = "UniquePrimaryfileNamePerItem", columnNames = {"primaryfile_id", "name"})})
@UniqueName
public class PrimaryfileSupplement extends Supplement {

	@NotNull
	@Index
	@ManyToOne
    private Primaryfile primaryfile;
    
}
