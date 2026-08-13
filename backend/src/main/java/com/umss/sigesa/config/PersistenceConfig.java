package com.umss.sigesa.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Persistencia JPA fuera de {@code SigesaApplication} para que los slices
 * {@code @WebMvcTest} no activen repositorios sin {@code entityManagerFactory}.
 */

