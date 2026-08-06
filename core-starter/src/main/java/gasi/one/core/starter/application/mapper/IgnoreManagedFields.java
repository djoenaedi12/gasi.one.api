package gasi.one.core.starter.application.mapper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.mapstruct.Mapping;

/**
 * MapStruct meta-annotation that protects starter-managed fields.
 *
 * @since 1.0.0
 */
@Retention(RetentionPolicy.CLASS)
@Mapping(target = "id", ignore = true)
@Mapping(target = "createdAt", ignore = true)
@Mapping(target = "updatedAt", ignore = true)
@Mapping(target = "createdBy", ignore = true)
@Mapping(target = "updatedBy", ignore = true)
@Mapping(target = "version", ignore = true)
@Mapping(target = "importBatchId", ignore = true)
@Mapping(target = "sourceId", ignore = true)
@Mapping(target = "lifecycleStatus", ignore = true)
public @interface IgnoreManagedFields {
}
