package edu.indiana.dlib.amppd.util;

import java.util.ArrayList;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.LRUMap;
import org.springframework.stereotype.Service;

@Service
public class CacheHelper {
 
    private LRUMap cacheMap;
 
    protected class CacheObject<T> {
        public long lastAccessed = System.currentTimeMillis();
        public T value;
        public long timeToLive;
        protected CacheObject(T value, long timeToLive) {
            this.value = value;
            this.timeToLive = timeToLive * 1000;
        }
    }
 
    public CacheHelper() {
    	init(100);
    }
    public CacheHelper(long timeToLive, final long timerInterval, int maxItems) {
        init(maxItems);
    }	
    public void init(int maxItems) {
        cacheMap = new LRUMap(maxItems);

        Thread t = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(30 * 1000);
                    } catch (InterruptedException ex) {
                    }
                    cleanup();
                }
            }
        });

        t.setDaemon(true);
        t.start();
    }
    public void put(String key, Object value, long timeToLive) {
        synchronized (cacheMap) {
        	cacheMap.put(key, new CacheObject(value, timeToLive));
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
        	System.out.println("Removing " + key + " from cache");
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
            	System.out.println("now: " + now + " last: " + (c.timeToLive + c.lastAccessed));
                if (c != null && (now > (c.timeToLive + c.lastAccessed))) {
                	System.out.println("Expiring " + key + " from cache");
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