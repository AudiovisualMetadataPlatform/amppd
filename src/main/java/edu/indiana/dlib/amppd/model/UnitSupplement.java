package edu.indiana.dlib.amppd.model;

import javax.jdo.annotations.Index;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Formula;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.indiana.dlib.amppd.validator.UniqueName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * UnitSupplement is a supplemental file associated with a unit and shared by all collections within that unit.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = {@UniqueConstraint(name = "UniqueUnitSupplementNamePerUnit", columnNames = {"unit_id", "name"})})
@UniqueName(message="Unit supplement name must be unique within its parent unit")
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class UnitSupplement extends Supplement {

	@NotNull
	@Index
	@ManyToOne
    private Unit unit;
		
	// true if there aren't any incomplete workflow invocation on its parent
    @Formula("not exists (select w.id from workflow_result w where w.unit_id = unit_id and w.status in ('SCHEDULED', 'IN_PROGRESS'))")
    private Boolean deletable;     	
	
	// Supplement other than PrimaryfileSupplement is never a groundtruth
    @Formula("false")
    private Boolean isGroundtruth; 
    
	@JsonIgnore
    public Long getAcUnitId() {
    	return unit.getAcUnitId();
    }
    
}
