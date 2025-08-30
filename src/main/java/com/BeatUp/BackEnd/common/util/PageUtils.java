package com.BeatUp.BackEnd.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageUtils {

    public static Pageable createPageable(int page, int size, String sortBy, String direction){
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
    }

    public static Pageable createDefaultPageable(int page, int size){
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public static Pageable createLatestPageable(int size){
        return PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

}

