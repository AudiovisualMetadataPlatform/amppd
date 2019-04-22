/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.indiana.dlib.amppd.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.service.impl.FileStorageServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FileStorageServiceTests {

	@Autowired
    private FileStorageService fileStorageService;

	// TODO test if below is not needed
//    @Before
//    public void init() {
//        fileStorageService = new FileStorageServiceImpl();
//    }

    @Test
    public void saveAndLoad() {
        fileStorageService.store(new MockMultipartFile("foo", "foo.txt", MediaType.TEXT_PLAIN_VALUE, "Test File Upload".getBytes()), "test");
        assertThat(fileStorageService.load("foo.txt")).exists();
    }

    @Test(expected = StorageException.class)
    public void saveNotPermitted() {
        fileStorageService.store(new MockMultipartFile("foo", "../foo.txt", MediaType.TEXT_PLAIN_VALUE, "Test File Upload".getBytes()), "test");
    }

    @Test
    public void savePermitted() {
        fileStorageService.store(new MockMultipartFile("foo", "bar/../foo.txt", MediaType.TEXT_PLAIN_VALUE, "Test File Upload".getBytes()), "test");
        assertThat(fileStorageService.load("bar/../foo.txt")).exists();
    }
    
    @Test
    public void getPrimaryfilePathName() {
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
        assertThat(pathname.startsWith("1/2/3/4."));
    }

    @Test
    public void getCollectionSupplementPathName() {
    	Unit unit = new Unit();
    	unit.setId(1);
    	
    	Collection collection = new Collection();
    	collection.setId(2);
    	collection.setUnit(unit);
    	    	
    	CollectionSupplement supplement = new CollectionSupplement();
    	supplement.setId(3);
    	supplement.setCollection(collection);
    	
    	String pathname = fileStorageService.getFilePathName(supplement); 	    
        assertThat(pathname.startsWith("1/2/3."));
    }
    
    @Test
    public void getItemSupplementPathName() {
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
        assertThat(pathname.startsWith("1/2/3/4."));
    }
    
    @Test
    public void getPrimaryfileSupplementPathName() {
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
        assertThat(pathname.startsWith("1/2/3/4/5."));
    }


}
