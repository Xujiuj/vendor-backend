package org.dromara.carbon.vendor.overview.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Vendor operations overview.
 */
@Data
public class CvOverviewVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<Metric> metrics;

    private AuthorizationChart authorizationChart;

    private List<Reminder> reminders;

    private List<Todo> todos;

    @Data
    public static class Metric implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String label;

        private Long value;

        private String note;
    }

    @Data
    public static class AuthorizationChart implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private List<String> months;

        private List<Series> series;
    }

    @Data
    public static class Series implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String name;

        private List<Long> values;
    }

    @Data
    public static class Reminder implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String title;

        private String description;
    }

    @Data
    public static class Todo implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String type;

        private String customer;

        private String description;

        private String action;

        private String path;
    }
}
