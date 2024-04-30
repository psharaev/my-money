package ru.psharaev.mymoney.core;

import ru.psharaev.mymoney.core.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface CategoriesRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
}
