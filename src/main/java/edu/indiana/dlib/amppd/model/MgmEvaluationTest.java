package edu.indiana.dlib.amppd.model;

import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * This class contains information about an MGM Evaluation Test (MET), i.e. an execution of the associated MGM Scoring Tool
 * to compute evaluation scores on a workflow result against its associated ground truth.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Table(indexes = {
		@Index(columnList = "mst_id"),
		@Index(columnList = "category_id"),
		@Index(columnList = "groundtruth_supplement_id"),
		@Index(columnList = "workflow_result_id"),
		@Index(columnList = "status"),
		@Index(columnList = "submitter"),
		@Index(columnList = "dateSubmitted")
})
@Data
public class MgmEvaluationTest {
	
	public enum TestStatus {
		RUNNING,
		SUCCESS,
		INVALID_GROUNDTRUTH,
		INVALID_WORKFLOW_RESULT,
		INVALID_PARAMETERS,
		SCRIPT_NOT_FOUND,
		RUNTIME_ERROR,
		OUTPUT_ERROR
	}
	
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
           
    // JSON representation of the MET parameters map as <name, value> pairs 
    // could be null if the test doesn't require parameters
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String parameters; 

    // MGM scoring tool used by this test
	@NotNull
	@ManyToOne
	private MgmScoringTool mst;

	// MGM category
	@NotNull
	@ManyToOne
	private MgmCategory category;

	// primaryfileSupplement used by this test as the groundtruth 
	@NotNull
	@ManyToOne
	// TODO below @OnDelete doesn't work, see workaround in MgmEvaluationService  
	@OnDelete(action = OnDeleteAction.CASCADE)	
    private PrimaryfileSupplement groundtruthSupplement;	
    
	// workflow result evaluated by this test
	@NotNull
	@ManyToOne
	// TODO below @OnDelete doesn't work, see workaround in WorkflowResultService
	@OnDelete(action = OnDeleteAction.CASCADE)	
    private WorkflowResult workflowResult; 

    // status of the test: running, success or failure with error code
	@NotNull
	@Enumerated(EnumType.STRING)
    private TestStatus status = TestStatus.RUNNING; 
    
 	// path of the output JSON score file, relative to the score root directory
	// could be null if the test failed
    @Type(type="text")
    private String scorePath;   

    // JSON representation of the output scores, could be null if the test failed
    // Note: The scores can also be stored in formats other than JSON, (ex. CSV or binary array of float numbers), 
    // depending on the need of visualization tools
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String scores;   
    
    // username of the AMP user who submitted the test
	@NotNull
	private String submitter;
	
	// timestamp when the test is submitted
	@NotNull
	private Date dateSubmitted;

	@Type(type="text")
	@Column(columnDefinition="text", length=10485760)
	private String mstErrorMsg;

	// <name, value> map of the parameters of the MET parsed from the parameters JSON
	@Transient
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private Map<String, Object> parametersMap;

}
