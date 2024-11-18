package edu.indiana.dlib.amppd.model.unused;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * MgmModeInput defines properties related to an output of an MGM mode.
 * @author yingfeng
 *
 */
//@Entity
//@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class MgmModeOutput extends MgmModeIo {

	@JsonIgnore
    public Long getAcUnitId() {
    	return null;
    }


}
