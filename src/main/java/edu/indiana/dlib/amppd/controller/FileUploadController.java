package edu.indiana.dlib.amppd.controller;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Primary;
import edu.indiana.dlib.amppd.model.Supplement;
import edu.indiana.dlib.amppd.repository.PrimaryRepository;
import edu.indiana.dlib.amppd.repository.SupplementRepository;
import edu.indiana.dlib.amppd.service.FileStorageService;

//TODO when we add controllers for data entities, we might want to move the actions into controllers for the associated entities.

/**
 * Controller to handle file upload for primaries and supplements.
 * @author yingfeng
 *
 */
@RestController
public class FileUploadController {
	
    private final FileStorageService fileStorageService;
    private final PrimaryRepository primaryRepository;
    private final SupplementRepository supplementRepository;

    @Autowired
    public FileUploadController(FileStorageService fileStorageService, PrimaryRepository primaryRepository, SupplementRepository supplementRepository) {
        this.fileStorageService = fileStorageService;
        this.primaryRepository = primaryRepository;
        this.supplementRepository = supplementRepository;
    }

    @PostMapping("/primary/{id}/file")
    public String handlePrimaryUpload(@PathParam("id") Long id, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
    	Primary primary = primaryRepository.findById(id).orElseThrow(() -> new StorageException("Primary <" + id + "> does not exist!"));
    	String targetPathName = fileStorageService.getFilePathName(primary);    	
    	
    	fileStorageService.store(file, targetPathName);
    	primary.setUri(targetPathName);
    	primaryRepository.save(primary);
    	
        redirectAttributes.addFlashAttribute("message", "You successfully uploaded primary " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }

    @PostMapping("/supplement/{id}/file")
    public String handleSupplementUpload(@PathParam("id") Long id, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
    	Supplement supplement = supplementRepository.findById(id).orElseThrow(() -> new StorageException("Supplement <" + id + "> does not exist!"));
    	String targetPathName = fileStorageService.getFilePathName(supplement);    	
    	
    	fileStorageService.store(file, targetPathName);
    	supplement.setUri(targetPathName);
    	supplementRepository.save(supplement);
    	
        redirectAttributes.addFlashAttribute("message", "You successfully uploaded supplement " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }
    
    
    
    
//  @GetMapping("/")
//  public String listUploadedFiles(Model model) throws IOException {
//
//      model.addAttribute("files", fileStorageService.loadAll().map(
//              path -> MvcUriComponentsBuilder.fromMethodName(PrimaryController.class,
//                      "serveFile", path.getFileName().toString()).build().toString())
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
