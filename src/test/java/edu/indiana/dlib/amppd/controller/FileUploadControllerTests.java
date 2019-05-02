package edu.indiana.dlib.amppd.controller;

import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
import edu.indiana.dlib.amppd.repository.CollectionSupplementRepository;
import edu.indiana.dlib.amppd.repository.ItemSupplementRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;
import edu.indiana.dlib.amppd.service.FileStorageService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class FileUploadControllerTests {

    @Autowired
    private MockMvc mvc;

	@Autowired
    private FileStorageService fileStorageService;

	@MockBean
	private PrimaryfileRepository primaryfileRepository;

	@MockBean
	private CollectionSupplementRepository collectionSupplementRepository;
	
	@MockBean
	private ItemSupplementRepository itemSupplementRepository;
	
	@MockBean
	private PrimaryfileSupplementRepository primaryfileSupplementRepository;
	
	// TODO: verify redirect for all following tests
	
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
    	
    	Mockito.when(primaryfileRepository.findById(4l)).thenReturn(Optional.of(primaryfile)); 
    	Mockito.when(primaryfileRepository.save(primaryfile)).thenReturn(primaryfile); 
    	
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test file upload".getBytes());
        this.mvc.perform(fileUpload("/primaryfiles/4/file").file(multipartFile))
                .andExpect(status().isOk());
//                .andExpect(header().string("Location", "/primaryfiles/4/file"));
//        then(fileStorageService).should().store(multipartFile, "U-1/C-2/I-3/P-4.mp4");
    }

    @Test
    public void shouldSaveUploadedCollectionSupplement() throws Exception {   	
    	Unit unit = new Unit();
    	unit.setId(1l);
    	
    	Collection collection = new Collection();
    	collection.setId(2l);
    	collection.setUnit(unit);
    	
    	CollectionSupplement collectionSupplement = new CollectionSupplement();
    	collectionSupplement.setId(3l);
    	collectionSupplement.setCollection(collection);  
    	collectionSupplement.setOriginalFilename("collectionsupplementtest.pdf");
    	
    	Mockito.when(collectionSupplementRepository.findById(3l)).thenReturn(Optional.of(collectionSupplement));
    	Mockito.when(collectionSupplementRepository.save(collectionSupplement)).thenReturn(collectionSupplement); 

    	MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test file upload".getBytes());
        this.mvc.perform(fileUpload("/collections/supplements/3/file").file(multipartFile))
                .andExpect(status().isOk());
//                .andExpect(header().string("Location", "/collections/supplements/3/file"));
//        then(fileStorageService).should().store(multipartFile, "U-1/C-2/S-3.pdf");
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
    	
    	ItemSupplement itemSupplement = new ItemSupplement();
    	itemSupplement.setId(4l);
    	itemSupplement.setItem(item);
    	itemSupplement.setOriginalFilename("itemsupplementtest.pdf");    

    	Mockito.when(itemSupplementRepository.findById(4l)).thenReturn(Optional.of(itemSupplement));
    	Mockito.when(itemSupplementRepository.save(itemSupplement)).thenReturn(itemSupplement);
    	
    	MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test file upload".getBytes());
        this.mvc.perform(fileUpload("/items/supplements/4/file").file(multipartFile))
                .andExpect(status().isOk());
//                .andExpect(header().string("Location", "/items/supplements/4/file"));
//        then(fileStorageService).should().store(multipartFile, "U-1/C-2/I-3/S-4.pdf");
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
    	
    	PrimaryfileSupplement primaryfileSupplement = new PrimaryfileSupplement();
    	primaryfileSupplement.setId(5l);
    	primaryfileSupplement.setPrimaryfile(primaryfile);
    	primaryfileSupplement.setOriginalFilename("primaryfilesupplementtest.pdf");

    	Mockito.when(primaryfileSupplementRepository.findById(5l)).thenReturn(Optional.of(primaryfileSupplement));
    	Mockito.when(primaryfileSupplementRepository.save(primaryfileSupplement)).thenReturn(primaryfileSupplement);

    	MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test file upload".getBytes());
        this.mvc.perform(fileUpload("/primaryfiles/supplements/5/file").file(multipartFile))
                .andExpect(status().isOk());
//                .andExpect(header().string("Location", "/primaryfiles/supplements/5/file"));
//        then(fileStorageService).should().store(multipartFile, "U-1/C-2/I-3/P-4/S-5.pdf");
    }

}
