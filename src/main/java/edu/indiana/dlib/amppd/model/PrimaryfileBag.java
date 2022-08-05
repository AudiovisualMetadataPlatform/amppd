package edu.indiana.dlib.amppd.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * PrimaryfileBag is the output bag for a primaryfile containing all the BagContents associated with the primaryfile.
 * @author yingfeng
 */
@Data
public class PrimaryfileBag {
	private Long primaryfileId;
	private String primaryfileName;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private List<BagContent> bagContents;
}
