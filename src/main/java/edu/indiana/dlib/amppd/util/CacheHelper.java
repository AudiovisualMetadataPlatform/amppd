package edu.indiana.dlib.amppd.util;

import java.util.ArrayList;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.LRUMap;
import org.springframework.stereotype.Service;

@Service
public class CacheHelper {
 
    private long timeToLive;
    private LRUMap cacheMap;
 
    protected class CacheObject<T> {
        public long lastAccessed = System.currentTimeMillis();
        public T value;
 
        protected CacheObject(T value) {
            this.value = value;
        }
    }
 
    public CacheHelper() {
    	init((long)120, 120, 100);
    }
    public CacheHelper(long timeToLive, final long timerInterval, int maxItems) {
        init(timeToLive, timerInterval, maxItems);
    }	
    public void init(long timeToLive, final long timerInterval, int maxItems) {
    	this.timeToLive = timeToLive * 1000;
    	 
        cacheMap = new LRUMap(maxItems);
 
        if (timeToLive > 0 && timerInterval > 0) {
 
            Thread t = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(timerInterval * 1000);
                        } catch (InterruptedException ex) {
                        }
                        cleanup();
                    }
                }
            });
 
            t.setDaemon(true);
            t.start();
        }
    }
    public void put(String key, Object value) {
        synchronized (cacheMap) {
        	cacheMap.put(key, new CacheObject(value));
        }
    }
 
    @SuppressWarnings("unchecked")
    public Object get(String key, boolean refresh) {
        synchronized (cacheMap) {
        	CacheObject c = (CacheObject) cacheMap.get(key);
 
            if (c == null)
                return null;
            else {
                if(refresh) c.lastAccessed = System.currentTimeMillis();
                return c.value;
            }
        }
    }
 
    public void remove(String key) {
        synchronized (cacheMap) {
        	cacheMap.remove(key);
        }
    }
 
    public int size() {
        synchronized (cacheMap) {
            return cacheMap.size();
        }
    }
 
    @SuppressWarnings("unchecked")
    public void cleanup() {
 
        long now = System.currentTimeMillis();
        ArrayList<String> deleteKey = null;
 
        synchronized (cacheMap) {
            MapIterator itr = cacheMap.mapIterator();
 
            deleteKey = new ArrayList<String>((cacheMap.size() / 2) + 1);
            String key = null;
            CacheObject c = null;
 
            while (itr.hasNext()) {
                key = (String) itr.next();
                c = (CacheObject) itr.getValue();
            	System.out.println("now: " + now + " last: " + (timeToLive + c.lastAccessed));
                if (c != null && (now > (timeToLive + c.lastAccessed))) {
                    deleteKey.add(key);
                }
            }
        }
 
        for (String key : deleteKey) {
            synchronized (cacheMap) {
            	cacheMap.remove(key);
            }
 
            Thread.yield();
        }
    }
}