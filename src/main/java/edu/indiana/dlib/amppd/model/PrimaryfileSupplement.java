package edu.indiana.dlib.amppd.model;

import javax.jdo.annotations.Index;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.indiana.dlib.amppd.validator.UniqueName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * PrimaryfileSupplement is a supplemental file associated with a primaryfile and only available for this primaryfile.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = {@UniqueConstraint(name = "UniquePrimaryfileSupplementNamePerPrimaryfile", columnNames = {"primaryfile_id", "name"})})
@UniqueName(message="Primaryfile supplement name must be unique within its parent primaryfile")
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class PrimaryfileSupplement extends Supplement {

	@NotNull
	@Index
	@ManyToOne
    private Primaryfile primaryfile;
    
	@JsonIgnore
    public Long getUnitId() {
    	return primaryfile.getAcUnitId();
    }
    
}
