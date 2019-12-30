package edu.indiana.dlib.amppd.model;

import java.util.Date;
import java.util.List;

/**
 * Class containing information of a batch manifest. Fields that apply to the whole spreadsheet such as collection name are entered via UI; 
 * while fields of each batch file comes from each row in the manifest spreadsheet.
 * @author yingfeng
 *
 */
public class Batch {
	AmpUser submitUser;
	Date submitTime;
	String manifestFilename;
	Collection collection;
	
	List<BatchFile> batchFiles;
}
