package com.aiplatform.shared.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CrudService<D, C, U> {
    D create(C command);
    D update(UUID id, U update);
    Optional<D> findById(UUID id);
    List<D> findAll(int page, int size);
    void delete(UUID id);
}
