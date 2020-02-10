package edu.indiana.dlib.amppd.service;

import edu.indiana.dlib.amppd.web.SaveTranscriptRequest;
import edu.indiana.dlib.amppd.web.TranscriptEditorRequest;
import edu.indiana.dlib.amppd.web.TranscriptEditorResponse;

/**
 * Service responsible for handling operation related to Human MGMS
 * @author dan
 *
 */
public interface HmgmService {

	/**
	 * Save a temporary copy of the transcript by adding the .tmp extension to the original file name
	 * @param request
	 * @return true or false depending on success
	 */
	boolean saveTranscript(SaveTranscriptRequest request);

	/**
	 * Complete edits of the transcript by copying the current state of the transcript to a new file with the .complete extension
	 * @param request
	 * @return
	 */
	boolean completeTranscript(TranscriptEditorRequest request);

	/**
	 * Get the transcript.  If a temporary version exists, get that version.  Reset will delete the temporary version and serve the original transcript. 
	 * @param datasetPath
	 * @param reset Resets the transcript to the original file.
	 * @return
	 */
	TranscriptEditorResponse getTranscript(String datasetPath, boolean reset);

}