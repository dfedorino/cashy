package com.dfedorino.cashy.service.exception.category;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(String categoryName) {
        super("Category not found by name: " + categoryName);
    }
}
