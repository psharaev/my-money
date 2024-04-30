package ru.psharaev.mymoney.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.psharaev.mymoney.core.entity.Category;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoriesRepository categoriesRepository;

    public Optional<Category> findCategory(Long categoryId) {
        return categoriesRepository.findById(categoryId);
    }

    @Transactional
    public Category getOrCreateCategory(String category) {
        return categoriesRepository.findByName(category)
                .orElseGet(() -> categoriesRepository.save(new Category(null, category)));

    }
}
