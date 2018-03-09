package br.com.geraldao.util;

import java.util.Collection;

import org.apache.log4j.Logger;

/**
 * 
 * @author victor
 *
 */
public class ListUtil {
    private final static Logger LOGGER = Logger.getLogger(ListUtil.class);

    /**
     * Checks if argument collection is null, empty or not loaded by JPA yet.
     * 
     * @param collection
     *            - Collection to check.
     * @return True if list is null, empty or not load yet. False otherwise.
     * 
     */
    public static <T> boolean isCollectionEmpty(Collection<T> collection) {
        if (collection == null) {
            return true;
        } else {
            try {
                return collection.isEmpty();
            } catch (Exception e) {
                // If not loaded by JPA yet, can throw PersistentBag exception or similar
                LOGGER.debug("Can't check collection, not loaded yet: " + e.getMessage());
                return true;
            }
        }
    }
}