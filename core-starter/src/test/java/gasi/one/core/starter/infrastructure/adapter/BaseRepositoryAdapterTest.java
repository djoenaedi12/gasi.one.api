package gasi.one.core.starter.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import gasi.one.core.api.common.exception.BusinessException;
import gasi.one.core.api.common.exception.ErrorDetail;
import gasi.one.core.api.common.query.SimpleFilter;
import gasi.one.core.api.common.query.SimpleFilter.FilterOperator;
import gasi.one.core.api.resource.model.BaseModel;
import gasi.one.core.starter.infrastructure.entity.BaseEntity;
import gasi.one.core.starter.infrastructure.mapper.BaseMapper;

class BaseRepositoryAdapterTest {

    @Test
    void findByConvertsNonUniqueResultToBusinessException() {
        TestRepository repository = mock(TestRepository.class);
        when(repository.findOne(any(Specification.class)))
                .thenThrow(new IncorrectResultSizeDataAccessException(1, 2));

        TestRepositoryAdapter adapter = new TestRepositoryAdapter(repository);

        assertThatThrownBy(() -> adapter.findBy(SimpleFilter.builder()
                .field("id")
                .operator(FilterOperator.EQUALS)
                .value(1L)
                .build()))
                .isInstanceOfSatisfying(BusinessException.class, ex -> assertThat(ex.getErrorDetails())
                        .extracting(ErrorDetail::getCode)
                        .containsExactly("QUERY_ONE_NOT_UNIQUE"));
    }

    private interface TestRepository extends JpaRepository<TestEntity, Long>,
            JpaSpecificationExecutor<TestEntity> {
    }

    private static final class TestRepositoryAdapter extends BaseRepositoryAdapter<TestModel, TestEntity> {

        private TestRepositoryAdapter(TestRepository repository) {
            super(repository, new TestMapper(), TestEntity.class, null);
        }

        @Override
        protected String resourceType() {
            return "test";
        }
    }

    static class TestModel extends BaseModel {
    }

    static class TestEntity extends BaseEntity {
    }

    private static final class TestMapper implements BaseMapper<TestModel, TestEntity> {

        @Override
        public TestModel toDomain(TestEntity entity) {
            return new TestModel();
        }

        @Override
        public TestEntity toEntity(TestModel domain) {
            return new TestEntity();
        }

        @Override
        public void updateEntity(TestModel source, TestEntity target) {
        }
    }
}
