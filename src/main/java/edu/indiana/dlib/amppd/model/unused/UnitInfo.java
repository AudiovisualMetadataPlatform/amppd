package edu.indiana.dlib.amppd.model.unused;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
public class UnitInfo {

	private Long id;
    private Date createdDate;
    private Date modifiedDate;
    private String createdBy;
    private String modifiedBy;   
    private String name;   
    private String description;
	private String taskManager;

	@JsonIgnore
    public Long getAcUnitId() {
    	return getId();
    }


}
