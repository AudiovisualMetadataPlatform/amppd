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


import static org.junit.Assert.assertTrue;

import java.nio.file.Files;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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

@RunWith(SpringRunner.class)
@SpringBootTest
public class FileStorageServiceTests {

	public static final String TEST_DIR_NAME = "test";
	
	@Autowired
    private FileStorageService fileStorageService;

    @After
    public void cleanAll() {
    	// clean up unit test directory after unit tests done
        fileStorageService.delete(TEST_DIR_NAME);
    }

    @Test
    public void saveAndLoad() {
        fileStorageService.store(new MockMultipartFile("foo", "foo.txt", MediaType.TEXT_PLAIN_VALUE, "Test File Upload".getBytes()), TEST_DIR_NAME + "/test.txt");
        assertTrue(Files.exists(fileStorageService.resolve("unit/test.txt")));
    }

    @Test(expected = StorageException.class)
    public void saveNotPermitted() {
        fileStorageService.store(new MockMultipartFile("foo", "../foo.txt", MediaType.TEXT_PLAIN_VALUE, "Test File Upload".getBytes()), TEST_DIR_NAME + "/test0.txt");
    }

    @Test
    public void savePermitted() {
        fileStorageService.store(new MockMultipartFile("foo", "bar/../foo.txt", MediaType.TEXT_PLAIN_VALUE, "Test File Upload".getBytes()), TEST_DIR_NAME + "/test1.txt");
        assertTrue(Files.exists(fileStorageService.resolve(TEST_DIR_NAME + "/test1.txt")));
    }
    
    @Test
    public void getPrimaryfilePathname() {
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
    	
    	String pathname = fileStorageService.getFilePathname(primaryfile);
    	assertTrue(pathname.equals("U-1/C-2/I-3/P-4.mp4"));
    }

    @Test
    public void getCollectionSupplementPathname() {
    	Unit unit = new Unit();
    	unit.setId(1l);
    	
    	Collection collection = new Collection();
    	collection.setId(2l);
    	collection.setUnit(unit);
    	    	
    	CollectionSupplement supplement = new CollectionSupplement();
    	supplement.setId(3l);
    	supplement.setCollection(collection);
    	supplement.setOriginalFilename("supplementtest.pdf");
  	
    	String pathname = fileStorageService.getFilePathname(supplement);
    	assertTrue(pathname.equals("U-1/C-2/S-3.pdf"));
    }
    
    @Test
    public void getItemSupplementPathname() {
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
    	
    	String pathname = fileStorageService.getFilePathname(supplement);
        assertTrue(pathname.equals("U-1/C-2/I-3/S-4.pdf"));
    }
    
    @Test
    public void getPrimaryfileSupplementPathname() {
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
    	
    	String pathname = fileStorageService.getFilePathname(supplement);
        assertTrue(pathname.equals("U-1/C-2/I-3/P-4/S-5.pdf"));
    }


}
