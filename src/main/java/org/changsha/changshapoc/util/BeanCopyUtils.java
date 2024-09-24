package org.changsha.changshapoc.util;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class BeanCopyUtils {
    public static <S, T> T copyObject(S source, Class<T> targetClazz) {
        try {
            if (source == null) {
                return null;
            }
            T target = targetClazz.newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <E, S extends List<E>, T> List<T> copyList(S source, Class<T> targetClazz) {
        try {
            if (source == null) {
                return null;
            }
            List<T> targetList = new ArrayList<>();
            for (E e : source) {
                T target = targetClazz.newInstance();
                BeanUtils.copyProperties(e, target);
                targetList.add(target);
            }
            return targetList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


