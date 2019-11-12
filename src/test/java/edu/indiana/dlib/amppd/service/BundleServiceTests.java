package edu.indiana.dlib.amppd.service;

import java.util.HashSet;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import edu.indiana.dlib.amppd.model.Bundle;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.BundleRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BundleServiceTests {
	
	@Autowired
    private BundleService bundleService;
	
    @MockBean
    private BundleRepository bundleRepository;
	
    @MockBean
    private PrimaryfileRepository primaryfileRepository;
	
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
    	
    	bundle = bundleService.addPrimaryfile(1l,1l);
    	Assert.assertTrue(bundle.getPrimaryfiles().size() == 1);
    	Assert.assertTrue(bundle.getPrimaryfiles().contains(primaryfile));
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
    	
    	bundle = bundleService.deletePrimaryfile(1l,1l);
    	Assert.assertTrue(bundle.getPrimaryfiles().size() == 0);
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
    	
    	bundle = bundleService.addPrimaryfile(1l,1l);
    	bundle = bundleService.addPrimaryfile(1l,1l);
    	Assert.assertTrue(bundle.getPrimaryfiles().size() == 1);
    	Assert.assertTrue(bundle.getPrimaryfiles().contains(primaryfile));	
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
    	
    	bundle = bundleService.deletePrimaryfile(1l,2l);
    	Assert.assertTrue(bundle.getPrimaryfiles().size() == 1);
    	Assert.assertTrue(bundle.getPrimaryfiles().contains(primaryfile));		
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
    	
    	Long[] primaryfileIds = {1l, 2l};
    	bundle = bundleService.addPrimaryfiles(0l, primaryfileIds);
    	Assert.assertTrue(bundle.getPrimaryfiles().size() == 2);
    	Assert.assertTrue(bundle.getPrimaryfiles().contains(primaryfile1));	
    	Assert.assertTrue(bundle.getPrimaryfiles().contains(primaryfile2));	
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
    	
    	Long[] primaryfileIds = {1l, 2l};
    	bundle = bundleService.deletePrimaryfiles(0l, primaryfileIds);
    	Assert.assertTrue(bundle.getPrimaryfiles().size() == 0);
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
    	
    	Long[] primaryfileIds = {1l, 1l};
    	bundle = bundleService.addPrimaryfiles(1l, primaryfileIds);
    	Assert.assertTrue(bundle.getPrimaryfiles().size() == 1);
    	Assert.assertTrue(bundle.getPrimaryfiles().contains(primaryfile));	 	
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
    	
    	Long[] primaryfileIds = {2l, 2l};
    	bundle = bundleService.deletePrimaryfiles(1l, primaryfileIds);
    	Assert.assertTrue(bundle.getPrimaryfiles().size() == 1);
    	Assert.assertTrue(bundle.getPrimaryfiles().contains(primaryfile));	 
    }
}
