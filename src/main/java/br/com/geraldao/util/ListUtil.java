package br.com.geraldao.util;

import java.util.Collection;

/**
 * 
 * @author victor
 *
 */
public class ListUtil {

    /**
     * Valida se a lista passada est� nula ou vazia.
     * 
     * @param collection
     *            - Lista que se deseja validar.
     * @return True se a lista estiver nula, vazia ou ainda n�o foi carregada pelo JPA. False se a lista n�o estiver vazia.
     * 
     */
    public static <T> boolean isCollectionEmpty(Collection<T> collection) {
        if (collection == null) {
            return true;
            // } else if (collection instanceof PersistentSet || collection
            // instanceof PersistentBag) {
        } else {
            try {
                return collection.isEmpty();
            } catch (Exception e) {
                // Caso ainda n�o carregada pelo JPA pode lan�ar exce��o
                // PersistentBag ou PersistentSet
                System.out.println("N�o foi possivel verificar collection, ela ainda n�o foi carregada:" + e.getMessage());
                return true;
            }
        }
    }
}