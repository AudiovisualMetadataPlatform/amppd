package edu.indiana.dlib.amppd.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
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
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.BundleRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class BundleControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BundleRepository bundleRepository;
	
    @MockBean
    private PrimaryfileRepository primaryfileRepository;
	
	// TODO: verify redirect for all following tests
	
    @Test
    public void shouldAddPrimaryfileToBundle() throws Exception {
    	Primaryfile primaryfile = new Primaryfile();
    	primaryfile.setId(1l);
    	primaryfile.setBundles(new HashSet<Bundle>());
    	
    	Bundle bundle = new Bundle();
    	bundle.setId(1l);
    	bundle.setPrimaryfiles(new HashSet<Primaryfile>());
    	
    	Mockito.when(primaryfileRepository.findById(1l)).thenReturn(Optional.of(primaryfile)); 
    	Mockito.when(primaryfileRepository.save(primaryfile)).thenReturn(primaryfile); 
    	Mockito.when(bundleRepository.findById(1l)).thenReturn(Optional.of(bundle)); 
    	Mockito.when(bundleRepository.save(bundle)).thenReturn(bundle); 
    	
    	mvc.perform(post("/bundles/1/addPrimaryfile?primaryfileId=1")).andExpect(status().isOk()).andExpect(
//				jsonPath("$.primaryfiles", hasSize(1))).andExpect(	// TODO need to import org.hamcrest.Matchers.hasSize with added dependency hamcrest-all
						jsonPath("$.primaryfiles[0].id").value(1));
    }

    @Test
    public void shouldDeletePrimaryfileFromBundle() throws Exception {   	
    	Primaryfile primaryfile = new Primaryfile();
    	primaryfile.setId(1l);
    	primaryfile.setBundles(new HashSet<Bundle>());
    	
    	Bundle bundle = new Bundle();
    	bundle.setId(1l);
    	bundle.setPrimaryfiles(new HashSet<Primaryfile>());
    	
    	primaryfile.getBundles().add(bundle);
    	bundle.getPrimaryfiles().add(primaryfile);    	
    	
    	Mockito.when(primaryfileRepository.findById(1l)).thenReturn(Optional.of(primaryfile)); 
    	Mockito.when(primaryfileRepository.save(primaryfile)).thenReturn(primaryfile); 
    	Mockito.when(bundleRepository.findById(1l)).thenReturn(Optional.of(bundle)); 
    	Mockito.when(bundleRepository.save(bundle)).thenReturn(bundle); 
    	
    	mvc.perform(post("/bundles/1/deletePrimaryfile?primaryfileId=1")).andExpect(status().isOk()).andExpect(
//				jsonPath("$.primaryfiles", hasSize(0))).andExpect( 
						jsonPath("$.primaryfiles[0].id").doesNotExist());    	
    }

    @Test
    public void shouldNotAddDuplicatePrimaryfileToBundle() throws Exception {   	
    	Primaryfile primaryfile = new Primaryfile();
    	primaryfile.setId(1l);
    	primaryfile.setBundles(new HashSet<Bundle>());
    	
    	Bundle bundle = new Bundle();
    	bundle.setId(1l);
    	bundle.setPrimaryfiles(new HashSet<Primaryfile>());
    	
    	primaryfile.getBundles().add(bundle);
    	bundle.getPrimaryfiles().add(primaryfile);    	
    	
    	Mockito.when(primaryfileRepository.findById(1l)).thenReturn(Optional.of(primaryfile)); 
    	Mockito.when(primaryfileRepository.save(primaryfile)).thenReturn(primaryfile); 
    	Mockito.when(bundleRepository.findById(1l)).thenReturn(Optional.of(bundle)); 
    	Mockito.when(bundleRepository.save(bundle)).thenReturn(bundle); 
    	
    	mvc.perform(post("/bundles/1/addPrimaryfile?primaryfileId=1"));
    	mvc.perform(post("/bundles/1/addPrimaryfile?primaryfileId=1")).andExpect(status().isOk()).andExpect(
//				jsonPath("$.primaryfiles", hasSize(1))).andExpect( 
				jsonPath("$.primaryfiles[0].id").value(1)).andExpect(
						jsonPath("$.primaryfiles[1].id").doesNotExist());    	
    }

    @Test
    public void shouldNotDeleteNonExistingPrimaryfileFromBundle() throws Exception {   	
    	Primaryfile primaryfile = new Primaryfile();
    	primaryfile.setId(1l);
    	primaryfile.setBundles(new HashSet<Bundle>());
    	
    	Primaryfile primaryfileDummy = new Primaryfile();
    	primaryfileDummy.setId(2l);
    	primaryfileDummy.setBundles(new HashSet<Bundle>());
    	
    	Bundle bundle = new Bundle();
    	bundle.setId(1l);
    	bundle.setPrimaryfiles(new HashSet<Primaryfile>());
    	
    	primaryfile.getBundles().add(bundle);
    	bundle.getPrimaryfiles().add(primaryfile);    	
    	
    	Mockito.when(primaryfileRepository.findById(2l)).thenReturn(Optional.of(primaryfileDummy)); 
    	Mockito.when(bundleRepository.findById(1l)).thenReturn(Optional.of(bundle)); 
    	
    	mvc.perform(post("/bundles/1/deletePrimaryfile?primaryfileId=2")).andExpect(status().isOk()).andExpect(
//				jsonPath("$.primaryfiles", hasSize(1))).andExpect( 
				jsonPath("$.primaryfiles[0].id").value(1));    	
    }  
    
    @Test
    public void shouldAddPrimaryfilesToBundle() throws Exception {
    	Primaryfile primaryfile1 = new Primaryfile();
    	primaryfile1.setId(1l);
    	primaryfile1.setBundles(new HashSet<Bundle>());
    	
    	Primaryfile primaryfile2 = new Primaryfile();
    	primaryfile2.setId(2l);
    	primaryfile2.setBundles(new HashSet<Bundle>());
    	
    	Bundle bundle = new Bundle();
    	bundle.setId(0l);
    	bundle.setPrimaryfiles(new HashSet<Primaryfile>());
    	
    	Mockito.when(primaryfileRepository.findById(1l)).thenReturn(Optional.of(primaryfile1)); 
    	Mockito.when(primaryfileRepository.save(primaryfile1)).thenReturn(primaryfile1); 
    	Mockito.when(primaryfileRepository.findById(2l)).thenReturn(Optional.of(primaryfile2)); 
    	Mockito.when(primaryfileRepository.save(primaryfile2)).thenReturn(primaryfile2); 
    	Mockito.when(bundleRepository.findById(0l)).thenReturn(Optional.of(bundle)); 
    	Mockito.when(bundleRepository.save(bundle)).thenReturn(bundle); 
    	
    	mvc.perform(post("/bundles/0/addPrimaryfiles?primaryfileIds=1,2")).andExpect(status().isOk()).andExpect(
//				jsonPath("$.primaryfiles", hasSize(1))).andExpect(	// TODO need to import org.hamcrest.Matchers.hasSize with added dependency hamcrest-all
						jsonPath("$.primaryfiles[0].id").isNumber()).andExpect( // we can't test the actual value as we don't know the order in which the primaryfiles are added
								jsonPath("$.primaryfiles[1].id").isNumber());
    	;
    }

    @Test
    public void shouldDeletePrimaryfilesFromBundle() throws Exception {   	
    	Primaryfile primaryfile1 = new Primaryfile();
    	primaryfile1.setId(1l);
    	primaryfile1.setBundles(new HashSet<Bundle>());
    	
    	Primaryfile primaryfile2 = new Primaryfile();
    	primaryfile2.setId(2l);
    	primaryfile2.setBundles(new HashSet<Bundle>());
    	
    	Bundle bundle = new Bundle();
    	bundle.setId(0l);
    	bundle.setPrimaryfiles(new HashSet<Primaryfile>());
    	    	
    	primaryfile1.getBundles().add(bundle);
    	primaryfile2.getBundles().add(bundle);
    	bundle.getPrimaryfiles().add(primaryfile1);    	
    	bundle.getPrimaryfiles().add(primaryfile2);    	
    	
    	Mockito.when(primaryfileRepository.findById(1l)).thenReturn(Optional.of(primaryfile1)); 
    	Mockito.when(primaryfileRepository.save(primaryfile1)).thenReturn(primaryfile1); 
    	Mockito.when(primaryfileRepository.findById(2l)).thenReturn(Optional.of(primaryfile2)); 
    	Mockito.when(primaryfileRepository.save(primaryfile2)).thenReturn(primaryfile2); 
    	Mockito.when(bundleRepository.findById(0l)).thenReturn(Optional.of(bundle)); 
    	Mockito.when(bundleRepository.save(bundle)).thenReturn(bundle);     	
    	
    	mvc.perform(post("/bundles/0/deletePrimaryfiles?primaryfileIds=1,2")).andExpect(status().isOk()).andExpect(
//				jsonPath("$.primaryfiles", hasSize(0))).andExpect( 
						jsonPath("$.primaryfiles[0].id").doesNotExist());    	
    }

    @Test
    public void shouldNotAddDuplicatePrimaryfilesToBundle() throws Exception {   	
    	Primaryfile primaryfile = new Primaryfile();
    	primaryfile.setId(1l);
    	primaryfile.setBundles(new HashSet<Bundle>());
    	
    	Bundle bundle = new Bundle();
    	bundle.setId(1l);
    	bundle.setPrimaryfiles(new HashSet<Primaryfile>());
    	
    	primaryfile.getBundles().add(bundle);
    	bundle.getPrimaryfiles().add(primaryfile);    	
    	
    	Mockito.when(primaryfileRepository.findById(1l)).thenReturn(Optional.of(primaryfile)); 
    	Mockito.when(primaryfileRepository.save(primaryfile)).thenReturn(primaryfile); 
    	Mockito.when(bundleRepository.findById(1l)).thenReturn(Optional.of(bundle)); 
    	Mockito.when(bundleRepository.save(bundle)).thenReturn(bundle); 
    	
    	mvc.perform(post("/bundles/1/addPrimaryfiles?primaryfileIds=1,1")).andExpect(status().isOk()).andExpect(
//				jsonPath("$.primaryfiles", hasSize(1))).andExpect( 
				jsonPath("$.primaryfiles[0].id").value(1)).andExpect(
						jsonPath("$.primaryfiles[1].id").doesNotExist());    	
    }

    @Test
    public void shouldNotDeleteNonExistingPrimaryfilesFromBundle() throws Exception {   	
    	Primaryfile primaryfile = new Primaryfile();
    	primaryfile.setId(1l);
    	primaryfile.setBundles(new HashSet<Bundle>());
    	
    	Primaryfile primaryfileDummy = new Primaryfile();
    	primaryfileDummy.setId(2l);
    	primaryfileDummy.setBundles(new HashSet<Bundle>());
    	
    	Bundle bundle = new Bundle();
    	bundle.setId(1l);
    	bundle.setPrimaryfiles(new HashSet<Primaryfile>());
    	
    	primaryfile.getBundles().add(bundle);
    	bundle.getPrimaryfiles().add(primaryfile);    	
    	
    	Mockito.when(primaryfileRepository.findById(2l)).thenReturn(Optional.of(primaryfileDummy)); 
    	Mockito.when(bundleRepository.findById(1l)).thenReturn(Optional.of(bundle)); 
    	
    	mvc.perform(post("/bundles/1/deletePrimaryfiles?primaryfileIds=2,2")).andExpect(status().isOk()).andExpect(
//				jsonPath("$.primaryfiles", hasSize(1))).andExpect( 
				jsonPath("$.primaryfiles[0].id").value(1));    	
    }
    
}
