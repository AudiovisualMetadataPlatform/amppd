package edu.indiana.dlib.amppd.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.Ignore;
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
import edu.indiana.dlib.amppd.model.UnitSupplement;
import edu.indiana.dlib.amppd.repository.CollectionSupplementRepository;
import edu.indiana.dlib.amppd.repository.ItemSupplementRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;
import edu.indiana.dlib.amppd.repository.UnitSupplementRepository;

// TODO remove ignore once we have ffmpeg and MediaProbe installed on Bamboo
@Ignore
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class FileUploadControllerTests {

	public static final Long TEST_ASSET_ID = 0l;

    @Autowired
    private MockMvc mvc;

	@MockBean
	private PrimaryfileRepository primaryfileRepository;

	@Autowired
	private UnitSupplementRepository unitSupplementRepository;
	
	@MockBean
	private CollectionSupplementRepository collectionSupplementRepository;
	
	@MockBean
	private ItemSupplementRepository itemSupplementRepository;
	
	@MockBean
	private PrimaryfileSupplementRepository primaryfileSupplementRepository;
	
    @Test
    public void shouldSaveUploadedPrimaryfile() throws Exception {
    	Unit unit = new Unit();
    	unit.setId(TEST_ASSET_ID);
    	
    	Collection collection = new Collection();
    	collection.setId(TEST_ASSET_ID);
    	collection.setUnit(unit);
    	
    	Item item = new Item();
    	item.setId(TEST_ASSET_ID);
    	item.setCollection(collection);
    	
    	Primaryfile primaryfile = new Primaryfile();
    	primaryfile.setId(TEST_ASSET_ID);
    	primaryfile.setItem(item);
    	
    	Mockito.when(primaryfileRepository.findById(TEST_ASSET_ID)).thenReturn(Optional.of(primaryfile)); 
    	Mockito.when(primaryfileRepository.save(primaryfile)).thenReturn(primaryfile); 
    	
        MockMultipartFile multipartFile = new MockMultipartFile("file", "primaryfiletest.mp4", "text/plain", "Test file upload".getBytes());
        mvc.perform(fileUpload("/primaryfiles/0/upload").file(multipartFile))
                .andExpect(status().isOk()).andExpect(
        				jsonPath("$.originalFilename").value("primaryfiletest.mp4")).andExpect(
        						jsonPath("$.pathname").value("U-0/C-0/I-0/P-0.mp4"));
    }

    @Test
    public void shouldSaveUploadedUnitSupplement() throws Exception {   	
    	Unit unit = new Unit();
    	unit.setId(TEST_ASSET_ID);
    	
    	UnitSupplement unitSupplement = new UnitSupplement();
    	unitSupplement.setId(TEST_ASSET_ID);
    	unitSupplement.setUnit(unit);  
    	
    	Mockito.when(unitSupplementRepository.findById(TEST_ASSET_ID)).thenReturn(Optional.of(unitSupplement));
    	Mockito.when(unitSupplementRepository.save(unitSupplement)).thenReturn(unitSupplement); 

    	MockMultipartFile multipartFile = new MockMultipartFile("file", "unitsupplementtest.pdf", "text/plain", "Test file upload".getBytes());
        mvc.perform(fileUpload("/unitsSupplements/0/upload").file(multipartFile))
                .andExpect(status().isOk()).andExpect(
        				jsonPath("$.originalFilename").value("unitsupplementtest.pdf")).andExpect(
        						jsonPath("$.pathname").value("U-0/S-0.pdf"));
    }

    @Test
    public void shouldSaveUploadedCollectionSupplement() throws Exception {   	
    	Unit unit = new Unit();
    	unit.setId(TEST_ASSET_ID);
    	
    	Collection collection = new Collection();
    	collection.setId(TEST_ASSET_ID);
    	collection.setUnit(unit);
    	
    	CollectionSupplement collectionSupplement = new CollectionSupplement();
    	collectionSupplement.setId(TEST_ASSET_ID);
    	collectionSupplement.setCollection(collection);  
    	
    	Mockito.when(collectionSupplementRepository.findById(TEST_ASSET_ID)).thenReturn(Optional.of(collectionSupplement));
    	Mockito.when(collectionSupplementRepository.save(collectionSupplement)).thenReturn(collectionSupplement); 

    	MockMultipartFile multipartFile = new MockMultipartFile("file", "collectionsupplementtest.pdf", "text/plain", "Test file upload".getBytes());
        mvc.perform(fileUpload("/collectionsSupplements/0/upload").file(multipartFile))
                .andExpect(status().isOk()).andExpect(
        				jsonPath("$.originalFilename").value("collectionsupplementtest.pdf")).andExpect(
        						jsonPath("$.pathname").value("U-0/C-0/S-0.pdf"));
    }

    @Test
    public void shouldSaveUploadedItemSupplement() throws Exception {
    	Unit unit = new Unit();
    	unit.setId(TEST_ASSET_ID);
    	
    	Collection collection = new Collection();
    	collection.setId(TEST_ASSET_ID);
    	collection.setUnit(unit);
    	
    	Item item = new Item();
    	item.setId(TEST_ASSET_ID);
    	item.setCollection(collection);
    	
    	ItemSupplement itemSupplement = new ItemSupplement();
    	itemSupplement.setId(TEST_ASSET_ID);
    	itemSupplement.setItem(item);

    	Mockito.when(itemSupplementRepository.findById(TEST_ASSET_ID)).thenReturn(Optional.of(itemSupplement));
    	Mockito.when(itemSupplementRepository.save(itemSupplement)).thenReturn(itemSupplement);
    	
    	MockMultipartFile multipartFile = new MockMultipartFile("file", "itemsupplementtest.pdf", "text/plain", "Test file upload".getBytes());
        mvc.perform(fileUpload("/itemsSupplements/0/upload").file(multipartFile))
                .andExpect(status().isOk()).andExpect(
        				jsonPath("$.originalFilename").value("itemsupplementtest.pdf")).andExpect(
        						jsonPath("$.pathname").value("U-0/C-0/I-0/S-0.pdf"));
    }

    @Test
    public void shouldSaveUploadedPrimaryfileSupplement() throws Exception {
    	Unit unit = new Unit();
    	unit.setId(TEST_ASSET_ID);
    	
    	Collection collection = new Collection();
    	collection.setId(TEST_ASSET_ID);
    	collection.setUnit(unit);
    	
    	Item item = new Item();
    	item.setId(TEST_ASSET_ID);
    	item.setCollection(collection);
    	
    	Primaryfile primaryfile = new Primaryfile();
    	primaryfile.setId(TEST_ASSET_ID);
    	primaryfile.setItem(item);
    	
    	PrimaryfileSupplement primaryfileSupplement = new PrimaryfileSupplement();
    	primaryfileSupplement.setId(TEST_ASSET_ID);
    	primaryfileSupplement.setPrimaryfile(primaryfile);

    	Mockito.when(primaryfileSupplementRepository.findById(TEST_ASSET_ID)).thenReturn(Optional.of(primaryfileSupplement));
    	Mockito.when(primaryfileSupplementRepository.save(primaryfileSupplement)).thenReturn(primaryfileSupplement);

    	MockMultipartFile multipartFile = new MockMultipartFile("file", "primaryfilesupplementtest.pdf", "text/plain", "Test file upload".getBytes());
        mvc.perform(fileUpload("/primaryfilesSupplements/0/upload").file(multipartFile))
                .andExpect(status().isOk()).andExpect(
        				jsonPath("$.originalFilename").value("primaryfilesupplementtest.pdf")).andExpect(
        						jsonPath("$.pathname").value("U-0/C-0/I-0/P-0/S-0.pdf"));
    }

}
