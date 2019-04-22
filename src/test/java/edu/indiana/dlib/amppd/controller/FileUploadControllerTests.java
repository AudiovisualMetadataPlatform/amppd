package edu.indiana.dlib.amppd.controller;

import java.nio.file.Paths;
import java.util.stream.Stream;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hello.storage.StorageFileNotFoundException;
import hello.storage.StorageService;

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
    	unit.setId(1);
    	
    	Collection collection = new Collection();
    	collection.setId(2);
    	collection.setUnit(unit);
    	
    	Item item = new Item();
    	item.setId(3);
    	item.setCollection(collection);
    	
    	Primaryfile primaryfile = new Primaryfile();
    	primaryfile.setId(4);
    	primaryfile.setItem(item);
    	
    	String pathname = fileStorageService.getFilePathName(primaryfile); 	    

        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test file upload".getBytes());
        this.mvc.perform(fileUpload("/primaryfile/4/file").file(multipartFile))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", pathname));

        then(fileStorageService).should().store(multipartFile, primaryfile);
    }

    @Test
    public void shouldSaveUploadedCollectionSupplement() throws Exception {
    	Unit unit = new Unit();
    	unit.setId(1);
    	
    	Collection collection = new Collection();
    	collection.setId(2);
    	collection.setUnit(unit);
    	
    	CollectionSupplement supplement = new CollectionSupplement();
    	supplement.setId(3);
    	supplement.setCollection(collection);
    	
    	String pathname = fileStorageService.getFilePathName(supplement); 	    

        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test file upload".getBytes());
        this.mvc.perform(fileUpload("/supplement/3/file").file(multipartFile))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", pathname));

        then(fileStorageService).should().store(multipartFile, supplement);
    }

    @Test
    public void shouldSaveUploadedItemSupplement() throws Exception {
    	Unit unit = new Unit();
    	unit.setId(1);
    	
    	Collection collection = new Collection();
    	collection.setId(2);
    	collection.setUnit(unit);
    	
    	Item item = new Item();
    	item.setId(3);
    	item.setCollection(collection);
    	
    	ItemSupplement supplement = new ItemSupplement();
    	supplement.setId(4);
    	supplement.setItem(item);
    	
    	String pathname = fileStorageService.getFilePathName(supplement); 	    

        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test file upload".getBytes());
        this.mvc.perform(fileUpload("/supplement/4/file").file(multipartFile))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", pathname));

        then(fileStorageService).should().store(multipartFile, supplement);
    }

    @Test
    public void shouldSaveUploadedPrimaryfileSupplement() throws Exception {
    	Unit unit = new Unit();
    	unit.setId(1);
    	
    	Collection collection = new Collection();
    	collection.setId(2);
    	collection.setUnit(unit);
    	
    	Item item = new Item();
    	item.setId(3);
    	item.setCollection(collection);
    	
    	Primaryfile primaryfile = new Primaryfile();
    	primaryfile.setId(4);
    	primaryfile.setItem(item);
    	
    	PrimaryfileSupplement supplement = new PrimaryfileSupplement();
    	supplement.setId(5);
    	supplement.setPrimaryfile(primaryfile);
    	
    	String pathname = fileStorageService.getFilePathName(supplement); 	    

        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test file upload".getBytes());
        this.mvc.perform(fileUpload("/supplement/5/file").file(multipartFile))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", pathname));

        then(fileStorageService).should().store(multipartFile, supplement);
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
