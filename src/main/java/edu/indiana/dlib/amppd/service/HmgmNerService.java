package edu.indiana.dlib.amppd.service;

import edu.indiana.dlib.amppd.web.NerEditorRequest;
import edu.indiana.dlib.amppd.web.NerEditorResponse;
import edu.indiana.dlib.amppd.web.SaveNerRequest;
import edu.indiana.dlib.amppd.web.SaveTranscriptRequest;
import edu.indiana.dlib.amppd.web.TranscriptEditorRequest;
import edu.indiana.dlib.amppd.web.TranscriptEditorResponse;

/**
 * Service responsible for handling operation related to Human MGMS NER Editor.
 * @yingfeng
 *
 */
public interface HmgmNerService {


	/**
	 * Get the IIIF manifest as the input resource for the NER Timeliner editor.  If a temporary version exists, get that version.  
	 * @param resourcePath absolute local path of the resource
	 * @return the content of the resource
	 */
	public String getNer(String resourcePath);

	/**
	 * Save a temporary copy of the manifest being edited in NER editor by adding the .tmp extension to the original file name.
	 * @param resourcePath absolute local path of the resource
	 * @param content the content of the edited file
	 * @return true or false depending on success
	 */
	public boolean saveNer(String resourcePath, String content);

	/**
	 * Complete edits of the manifest by copying the current saved tmp file to a new file with the .complete extension
	 * @param resourcePath absolute local path of the resource
	 * @return true or false depending on success
	 */
	public boolean completeNer(String resourcePath);

	/**
	 * Delete the temporary version file saved by NER editor, so that the original resource will be used next time the editor is reloaded.
	 * @param resourcePath absolute local path of the resource
	 * @return true or false depending on success
	 */
	public boolean resetNer(String resourcePath);

	
}