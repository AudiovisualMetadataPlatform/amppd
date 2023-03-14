package edu.indiana.dlib.amppd.model.dto;

import edu.indiana.dlib.amppd.model.AmpUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class for AmpUser projection, used for role assignment.
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

}
