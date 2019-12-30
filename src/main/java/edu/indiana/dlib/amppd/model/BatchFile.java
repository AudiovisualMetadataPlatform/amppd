package edu.indiana.dlib.amppd.model;

/**
 * Class containing information of a batch file. This corresponds to a row in a batch manifest spreadsheet.
 * @author yingfeng
 *
 */
public class BatchFile {	
	public enum FileType { PRIMARYFILE, COLLECTION_SUPPLEMENT, ITEM_SUPPLEMENT, PRIMARYFILE_SUPPLEMENT };

	String sourceIdType;
	String sourceId;
	String itemName;
	String itemDescription;
	FileType fileType; 
	String primaryfileFilename;
	String primaryfileName;
	String primaryfileDescription;
	String supplementFilename;	
}
