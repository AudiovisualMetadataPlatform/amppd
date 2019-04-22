package edu.indiana.dlib.amppd.model;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * PrimaryfileSupplement is a supplemental file associated with a primaryfile and only available for this primaryfile.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class PrimaryfileSupplement extends Supplement {

	@ManyToOne
    private Primaryfile primaryfile;
    
}
