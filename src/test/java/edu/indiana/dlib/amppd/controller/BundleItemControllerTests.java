package edu.indiana.dlib.amppd.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

import edu.indiana.dlib.amppd.model.Bundle;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.repository.BundleRepository;
import edu.indiana.dlib.amppd.repository.ItemRepository;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class BundleItemControllerTests {

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
    	
    	mvc.perform(post("/bundles/1/add/items/1")).andExpect(status().isOk()).andExpect(
//				jsonPath("$.items", hasSize(1))).andExpect(	// TODO need to import org.hamcrest.Matchers.hasSize with added dependency hamcrest-all
						jsonPath("$.items[0].id").value(1));
    }

    @Test
    public void shouldSaveUploadedBundleSupplement() throws Exception {   	
    	Item item = new Item();
    	item.setId(1l);
    	item.setBundles(new ArrayList<Bundle>());
    	
    	Bundle bundle = new Bundle();
    	bundle.setId(1l);
    	bundle.setItems(new ArrayList<Item>());
    	
    	item.getBundles().add(bundle);
    	bundle.getItems().add(item);    	
    	
    	Mockito.when(itemRepository.findById(1l)).thenReturn(Optional.of(item)); 
    	Mockito.when(itemRepository.save(item)).thenReturn(item); 
    	Mockito.when(bundleRepository.findById(1l)).thenReturn(Optional.of(bundle)); 
    	Mockito.when(bundleRepository.save(bundle)).thenReturn(bundle); 
    	
    	mvc.perform(post("/bundles/1/delete/items/1")).andExpect(status().isOk()).andExpect(
//				jsonPath("$.items", hasSize(0))).andExpect( 
						jsonPath("$.items[0].id").doesNotExist());    	
    }

}
