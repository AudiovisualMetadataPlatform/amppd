package edu.indiana.dlib.amppd.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.indiana.dlib.amppd.model.Dataentity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class for Unit brief DTO.
 * @author yingfeng
 */
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class UnitInfo extends Dataentity  {

	private String taskManager;

	@JsonIgnore
    public Long getAcUnitId() {
    	return getId();
    }


}
