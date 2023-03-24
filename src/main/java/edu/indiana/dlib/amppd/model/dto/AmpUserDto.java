package edu.indiana.dlib.amppd.model.dto;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.projection.AmpUserBrief;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class for AmpUser brief DTO.
 * @author yingfeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmpUserDto {

	private Long id;
	private String username;
	private String firstName;
	private String lastName;

	public AmpUserDto(AmpUser user) {
		id = user.getId();
		username = user.getUsername();
		firstName = user.getFirstName();
		lastName = user.getLastName();
	}

	public AmpUserDto(AmpUserBrief user) {
		id = user.getId();
		username = user.getUsername();
		firstName = user.getFirstName();
		lastName = user.getLastName();
	}

}
