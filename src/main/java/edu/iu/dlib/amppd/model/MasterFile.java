package edu.iu.dlib.amppd.model;

/**
 * MasterFile is a file containing actual media content of any MIME type. A master file always associates with one and only one item.
 * @author yingfeng
 *
 */
public class MasterFile extends MediaFile {

    private Long itemId;
    private Item item;
    
}
