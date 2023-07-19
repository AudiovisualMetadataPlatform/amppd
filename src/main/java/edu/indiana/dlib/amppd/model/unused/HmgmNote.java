package edu.indiana.dlib.amppd.model.unused;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class representing a Human MGM task note, which is added by a task assignee or a supervisor for communication.
 * @author yingfeng
 *
 */
//@Entity
//@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class HmgmNote {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @CreatedDate
    private long createdDate;
    
    @CreatedBy
    private String createdBy;

    // the actual content of the note
    private String text;
    
	@ManyToOne 
	private HmgmTask hmgmTask;

}
