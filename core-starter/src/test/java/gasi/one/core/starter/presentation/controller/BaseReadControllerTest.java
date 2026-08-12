package gasi.one.core.starter.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import gasi.one.core.api.common.dto.PageResult;
import gasi.one.core.api.common.exception.BadRequestException;
import gasi.one.core.api.common.exception.BusinessException;
import gasi.one.core.api.common.exception.ErrorDetail;
import gasi.one.core.api.common.id.IdCodec;
import gasi.one.core.api.common.query.GenericFilter;
import gasi.one.core.api.common.query.QueryRequest;
import gasi.one.core.api.common.query.SortOrder;
import gasi.one.core.api.resource.port.inbound.BaseReadService;

class BaseReadControllerTest {

    @Test
    void findByRejectsMissingFilter() {
        TestController controller = new TestController();

        assertThatThrownBy(() -> controller.findBy(new QueryRequest()))
                .isInstanceOfSatisfying(BusinessException.class, ex -> assertThat(ex.getErrorDetails())
                        .extracting(ErrorDetail::getCode)
                        .containsExactly("QUERY_ONE_FILTER_REQUIRED"));
    }

    @Test
    void findByIdRejectsInvalidId() {
        TestController controller = new TestController();

        assertThatThrownBy(() -> controller.findById("not-a-valid-id"))
                .isInstanceOfSatisfying(BadRequestException.class, ex -> assertThat(ex.getErrorDetails())
                        .extracting(ErrorDetail::getCode)
                        .containsExactly("INVALID_ID"));
    }

    private static final class TestController extends BaseReadController<Object, Object> {

        private TestController() {
            super(new TestReadService(), new TestIdCodec(), null);
        }

        @Override
        public String resourceType() {
            return "TEST";
        }
    }

    private static final class TestReadService implements BaseReadService<Object, Object> {

        @Override
        public Object findById(Long id) {
            return null;
        }

        @Override
        public Object findBy(GenericFilter filter) {
            return null;
        }

        @Override
        public List<Object> findAll(GenericFilter filter, List<SortOrder> orders) {
            return List.of();
        }

        @Override
        public PageResult<Object> findAll(int page, int size, GenericFilter filter, List<SortOrder> orders) {
            return PageResult.<Object>builder()
                    .content(List.of())
                    .page(page)
                    .size(size)
                    .totalElements(0)
                    .totalPages(0)
                    .build();
        }
    }

    private static final class TestIdCodec implements IdCodec {

        @Override
        public String encode(Long id) {
            return id == null ? null : id.toString();
        }

        @Override
        public Long decode(String hash) {
            return hash == null ? null : Long.valueOf(hash);
        }

        @Override
        public List<Long> decodeList(List<String> encodedIds) {
            return encodedIds == null ? null : encodedIds.stream().map(this::decode).toList();
        }

        @Override
        public List<String> encodeList(List<Long> ids) {
            return ids == null ? null : ids.stream().map(this::encode).toList();
        }
    }
}
