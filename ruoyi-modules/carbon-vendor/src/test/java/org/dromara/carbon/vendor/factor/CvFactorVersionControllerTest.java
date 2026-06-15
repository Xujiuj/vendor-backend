package org.dromara.carbon.vendor.factor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.carbon.vendor.controller.CvFactorVersionController;
import org.dromara.carbon.vendor.service.ICvFactorVersionService;
import org.dromara.common.core.domain.R;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@Tag("dev")
class CvFactorVersionControllerTest {

    @Test
    void retireDelegatesToServiceWithServerSideOperator() {
        ICvFactorVersionService service = mock(ICvFactorVersionService.class);
        CvFactorVersionController controller = new CvFactorVersionController(service);

        R<Void> response = controller.retire(101L);

        assertEquals(R.SUCCESS, response.getCode());
        verify(service).retireFactorVersion(101L, "vendor-system");
    }

    @Test
    void restoreDelegatesToServiceWithServerSideOperator() {
        ICvFactorVersionService service = mock(ICvFactorVersionService.class);
        CvFactorVersionController controller = new CvFactorVersionController(service);

        R<Void> response = controller.restore(101L);

        assertEquals(R.SUCCESS, response.getCode());
        verify(service).restoreFactorVersion(101L, "vendor-system");
    }

    @Test
    void retireEndpointUsesEditPermissionAndPostMapping() throws Exception {
        Method method = CvFactorVersionController.class.getMethod("retire", Long.class);
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        SaCheckPermission permission = method.getAnnotation(SaCheckPermission.class);

        assertNotNull(postMapping);
        assertNotNull(permission);
        assertArrayEquals(new String[] {"/{id}/retire"}, postMapping.value());
        assertArrayEquals(new String[] {"vendor:factorVersion:edit"}, permission.value());
    }

    @Test
    void restoreEndpointUsesEditPermissionAndPostMapping() throws Exception {
        Method method = CvFactorVersionController.class.getMethod("restore", Long.class);
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        SaCheckPermission permission = method.getAnnotation(SaCheckPermission.class);

        assertNotNull(postMapping);
        assertNotNull(permission);
        assertArrayEquals(new String[] {"/{id}/restore"}, postMapping.value());
        assertArrayEquals(new String[] {"vendor:factorVersion:edit"}, permission.value());
    }
}
