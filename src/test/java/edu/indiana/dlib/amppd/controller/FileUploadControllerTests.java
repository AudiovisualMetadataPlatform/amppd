package edu.indiana.dlib.amppd.controller;

import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.service.FileStorageService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class FileUploadControllerTests {

    @Autowired
    private MockMvc mvc;

	@Autowired
    private FileStorageService fileStorageService;

    @Test
    public void shouldSaveUploadedPrimaryfile() throws Exception {
    	Unit unit = new Unit();
    	unit.setId(1l);
    	
    	Collection collection = new Collection();
    	collection.setId(2l);
    	collection.setUnit(unit);
    	
    	Item item = new Item();
    	item.setId(3l);
    	item.setCollection(collection);
    	
    	Primaryfile primaryfile = new Primaryfile();
    	primaryfile.setId(4l);
    	primaryfile.setItem(item);
    	primaryfile.setOriginalFilename("primaryfiletest.mp4");   

        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test file upload".getBytes());
        this.mvc.perform(fileUpload("/primaryfile/4/file").file(multipartFile))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "U-1/C-2/I-3/P-4.mp4"));

        then(fileStorageService).should().store(multipartFile, "U-1/C-2/I-3/P-4.mp4");
    }

    @Test
    public void shouldSaveUploadedCollectionSupplement() throws Exception {
    	Unit unit = new Unit();
    	unit.setId(1l);
    	
    	Collection collection = new Collection();
    	collection.setId(2l);
    	collection.setUnit(unit);
    	
    	CollectionSupplement supplement = new CollectionSupplement();
    	supplement.setId(3l);
    	supplement.setCollection(collection);  
    	supplement.setOriginalFilename("supplementtest.pdf");
    	
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test file upload".getBytes());
        this.mvc.perform(fileUpload("/supplement/3/file").file(multipartFile))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "U-1/C-2/S-3.pdf"));

        then(fileStorageService).should().store(multipartFile, "U-1/C-2/S-3.pdf");
    }

    @Test
    public void shouldSaveUploadedItemSupplement() throws Exception {
    	Unit unit = new Unit();
    	unit.setId(1l);
    	
    	Collection collection = new Collection();
    	collection.setId(2l);
    	collection.setUnit(unit);
    	
    	Item item = new Item();
    	item.setId(3l);
    	item.setCollection(collection);
    	
    	ItemSupplement supplement = new ItemSupplement();
    	supplement.setId(4l);
    	supplement.setItem(item);
    	supplement.setOriginalFilename("supplementtest.pdf");    

        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test file upload".getBytes());
        this.mvc.perform(fileUpload("/supplement/4/file").file(multipartFile))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "U-1/C-2/I-3/S-4.pdf"));

        then(fileStorageService).should().store(multipartFile, "U-1/C-2/I-3/S-4.pdf");
    }

    @Test
    public void shouldSaveUploadedPrimaryfileSupplement() throws Exception {
    	Unit unit = new Unit();
    	unit.setId(1l);
    	
    	Collection collection = new Collection();
    	collection.setId(2l);
    	collection.setUnit(unit);
    	
    	Item item = new Item();
    	item.setId(3l);
    	item.setCollection(collection);
    	
    	Primaryfile primaryfile = new Primaryfile();
    	primaryfile.setId(4l);
    	primaryfile.setItem(item);
    	
    	PrimaryfileSupplement supplement = new PrimaryfileSupplement();
    	supplement.setId(5l);
    	supplement.setPrimaryfile(primaryfile);
    	supplement.setOriginalFilename("supplementtest.pdf");

        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test file upload".getBytes());
        this.mvc.perform(fileUpload("/supplement/5/file").file(multipartFile))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "U-1/C-2/I-3/P-4/S-5.pdf"));

        then(fileStorageService).should().store(multipartFile, "U-1/C-2/I-3/P-4/S-5.pdf");
    }

//    @SuppressWarnings("unchecked")
//    @Test
//    public void should404WhenMissingFile() throws Exception {
//        given(this.fileStorageService.loadAsResource("test.txt"))
//                .willThrow(StorageFileNotFoundException.class);
//
//        this.mvc.perform(get("/files/test.txt")).andExpect(status().isNotFound());
//    }

}
