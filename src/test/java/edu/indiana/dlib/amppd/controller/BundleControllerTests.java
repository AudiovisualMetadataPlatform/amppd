package edu.indiana.dlib.amppd.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import edu.indiana.dlib.amppd.model.Bundle;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.repository.BundleRepository;
import edu.indiana.dlib.amppd.repository.ItemRepository;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class BundleControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BundleRepository bundleRepository;
	
    @MockBean
    private ItemRepository itemRepository;
	
	// TODO: verify redirect for all following tests
	
    @Test
    public void shouldAddItemToBundle() throws Exception {
    	Item item = new Item();
    	item.setId(1l);
    	item.setBundles(new ArrayList<Bundle>());
    	
    	Bundle bundle = new Bundle();
    	bundle.setId(1l);
    	bundle.setItems(new ArrayList<Item>());
    	
    	Mockito.when(itemRepository.findById(1l)).thenReturn(Optional.of(item)); 
    	Mockito.when(itemRepository.save(item)).thenReturn(item); 
    	Mockito.when(bundleRepository.findById(1l)).thenReturn(Optional.of(bundle)); 
    	Mockito.when(bundleRepository.save(bundle)).thenReturn(bundle); 
    	
//    	mvc.perform(post("/bundles/1/add/items/1").andExpect(status().isOk())
    	
//        this.mvc.perform(fileUpload("/items/4/file").file(multipartFile))
//                .andExpect(status().isOk());
////                .andExpect(header().string("Location", "/items/4/file"));
////        then(fileStorageService).should().store(multipartFile, "U-1/C-2/I-3/P-4.mp4");
    }

    @Test
    public void shouldSaveUploadedBundleSupplement() throws Exception {   	
//    	Unit unit = new Unit();
//    	unit.setId(1l);
//    	
//    	Bundle bundle = new Bundle();
//    	bundle.setId(2l);
//    	bundle.setUnit(unit);
//    	
//    	BundleSupplement bundleSupplement = new BundleSupplement();
//    	bundleSupplement.setId(3l);
//    	bundleSupplement.setBundle(bundle);  
//    	bundleSupplement.setOriginalFilename("bundlesupplementtest.pdf");
//    	
//    	Mockito.when(bundleSupplementRepository.findById(3l)).thenReturn(Optional.of(bundleSupplement));
//    	Mockito.when(bundleSupplementRepository.save(bundleSupplement)).thenReturn(bundleSupplement); 
//
//    	MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test file upload".getBytes());
//        this.mvc.perform(fileUpload("/bundles/supplements/3/file").file(multipartFile))
//                .andExpect(status().isOk());
////                .andExpect(header().string("Location", "/bundles/supplements/3/file"));
////        then(fileStorageService).should().store(multipartFile, "U-1/C-2/S-3.pdf");
    }

}
