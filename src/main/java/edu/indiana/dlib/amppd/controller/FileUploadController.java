package edu.indiana.dlib.amppd.controller;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.Supplement;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.SupplementRepository;
import edu.indiana.dlib.amppd.service.FileStorageService;

// TODO: when we add controllers for data entities, we might want to move the actions into controllers for the associated entities.

/**
 * Controller to handle file upload for primaryfiles and supplements.
 * @author yingfeng
 *
 */
@RestController
public class FileUploadController {
	
	@Autowired
    private FileStorageService fileStorageService;
	
	@Autowired
    private PrimaryfileRepository primaryfileRepository;
	
	@Autowired
    private SupplementRepository supplementRepository;

    @PostMapping("/primaryfile/{id}/file")
    public String handlePrimaryfileUpload(@PathParam("id") Long id, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {		
    	Primaryfile primaryfile = primaryfileRepository.findById(id).orElseThrow(() -> new StorageException("Primaryfile <" + id + "> does not exist!"));    
    	primaryfile.setOriginalFilename(StringUtils.cleanPath(file.getOriginalFilename()));	
    	String targetPathname = fileStorageService.getFilePathname(primaryfile);    	    	
    	primaryfile.setPathname(targetPathname);
    	fileStorageService.store(file, targetPathname);
    	
//    	if (StringUtils.isEmpty(primaryfile.getDescription())) {
//		primaryfile.setDescription(FilenameUtils.getBaseName(originalFilename));	
//	}
	    	
    	primaryfileRepository.save(primaryfile);    	
        redirectAttributes.addFlashAttribute("message", "You successfully uploaded primaryfile " + file.getOriginalFilename() + " to " + targetPathname + "!");
        return "redirect:/";
    }

    @PostMapping("/supplement/{id}/file")
    public String handleSupplementUpload(@PathParam("id") Long id, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {		
    	Supplement supplement = supplementRepository.findById(id).orElseThrow(() -> new StorageException("Supplement <" + id + "> does not exist!"));
    	supplement.setOriginalFilename(StringUtils.cleanPath(file.getOriginalFilename()));
    	String targetPathname = fileStorageService.getFilePathname(supplement);    		    	
    	supplement.setPathname(targetPathname);
    	fileStorageService.store(file, targetPathname);

//    	if (StringUtils.isEmpty(supplement.getDescription())) {
//		supplement.setDescription(FilenameUtils.getBaseName(originalFilename));
//	}
	    	
    	supplementRepository.save(supplement);   	
        redirectAttributes.addFlashAttribute("message", "You successfully uploaded supplement " + file.getOriginalFilename() + " to " + targetPathname + "!");
        return "redirect:/";
    }
    
    
    
    
//  @GetMapping("/")
//  public String listUploadedFiles(Model model) throws IOException {
//
//      model.addAttribute("files", fileStorageService.loadAll().map(
//              path -> MvcUriComponentsBuilder.fromMethodName(PrimaryController.class,
//                      "serveFile", path.getFilename().toString()).build().toString())
//              .collect(Collectors.toList()));
//
//      return "uploadForm";
//  }

//  @GetMapping("/files/{filename:.+}")
//  @ResponseBody
//  public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
//
//      Resource file = fileStorageService.loadAsResource(filename);
//      return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
//              "attachment; filename=\"" + file.getFilename() + "\"").body(file);
//  }    

//    @ExceptionHandler(StorageFileNotFoundException.class)
//    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
//        return ResponseEntity.notFound().build();
//    }

}
