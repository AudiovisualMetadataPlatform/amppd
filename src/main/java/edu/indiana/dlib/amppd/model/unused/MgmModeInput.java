package edu.indiana.dlib.amppd.model.unused;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * MgmModeInput defines properties related to an input of an MGM mode.
 * @author yingfeng
 *
 */
//@Entity
//@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class MgmModeInput extends MgmModeIo {

	@JsonIgnore
	public Long getAcUnitId() {
		return null;
	}


}
