package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvCustomer;
import org.dromara.carbon.vendor.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.domain.CvRenewalOrder;
import org.dromara.carbon.vendor.domain.CvReportTemplateDownloadToken;
import org.dromara.carbon.vendor.domain.vo.CvOverviewVo;
import org.dromara.carbon.vendor.mapper.CvCustomerMapper;
import org.dromara.carbon.vendor.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.mapper.CvRenewalOrderMapper;
import org.dromara.carbon.vendor.mapper.CvReportTemplateDownloadTokenMapper;
import org.dromara.carbon.vendor.service.ICvOverviewService;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Vendor operations overview service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvOverviewServiceImpl implements ICvOverviewService {

    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_TRIAL = "trial";
    private static final String STATUS_FORMAL = "formal";
    private static final String ISSUE_STATUS_ISSUED = "issued";
    private static final String ISSUE_STATUS_REVOKED = "revoked";
    private static final String TOKEN_STATUS_ISSUED = "issued";
    private static final String ORDER_STATUS_PENDING = "pending";
    private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();

    private final CvCustomerMapper customerMapper;
    private final CvLicenseIssueMapper licenseIssueMapper;
    private final CvReportTemplateDownloadTokenMapper downloadTokenMapper;
    private final CvRenewalOrderMapper renewalOrderMapper;

    @Override
    public CvOverviewVo queryOverview() {
        Date now = new Date();
        Date inThirtyDays = daysAfter(now, 30);
        Date monthStart = monthStart(now);

        CvOverviewVo overview = new CvOverviewVo();
        overview.setMetrics(buildMetrics(now, inThirtyDays, monthStart));
        overview.setAuthorizationChart(buildAuthorizationChart(now));
        overview.setReminders(buildReminders(now, inThirtyDays));
        overview.setTodos(buildTodos(now, inThirtyDays));
        return overview;
    }

    private List<CvOverviewVo.Metric> buildMetrics(Date now, Date inThirtyDays, Date monthStart) {
        long activeCustomers = countActiveCustomers();
        long trialCustomers = countCustomersByStatus(STATUS_TRIAL);
        long formalCustomers = countCustomersByStatus(STATUS_FORMAL);
        long expiringLicenses = countIssuedLicenses(Wrappers.<CvLicenseIssue>lambdaQuery()
            .gt(CvLicenseIssue::getValidTo, now)
            .le(CvLicenseIssue::getValidTo, inThirtyDays));
        long activeLicenses = countIssuedLicenses(Wrappers.<CvLicenseIssue>lambdaQuery()
            .le(CvLicenseIssue::getValidFrom, now)
            .gt(CvLicenseIssue::getValidTo, now));
        long expiredLicenses = countIssuedLicenses(Wrappers.<CvLicenseIssue>lambdaQuery()
            .le(CvLicenseIssue::getValidTo, now));
        long monthlyTemplateDownloads = downloadTokenMapper.selectCount(Wrappers.<CvReportTemplateDownloadToken>lambdaQuery()
            .ge(CvReportTemplateDownloadToken::getCreateTime, monthStart));
        long totalTemplateDownloads = downloadTokenMapper.selectCount(Wrappers.lambdaQuery());

        return Arrays.asList(
            metric("有效客户", activeCustomers, "试用 " + trialCustomers + " · 正式 " + formalCustomers),
            metric("30 天内到期", expiringLicenses, "待续费跟进"),
            metric("有效授权", activeLicenses, "临期 " + expiringLicenses + " · 已过期 " + expiredLicenses),
            metric("本月模板分发", monthlyTemplateDownloads, "累计 " + totalTemplateDownloads + " 次")
        );
    }

    private CvOverviewVo.AuthorizationChart buildAuthorizationChart(Date now) {
        List<Date> monthStarts = lastMonthStarts(now, 6);
        List<String> months = monthStarts.stream().map(this::formatMonth).toList();
        List<String> editions = List.of("集团版", "专业版", "标准版");
        Map<String, String> editionNames = Map.of(
            "group", "集团版",
            "enterprise", "集团版",
            "professional", "专业版",
            "pro", "专业版",
            "standard", "标准版"
        );
        Map<String, Integer> editionOrder = Map.of("集团版", 0, "专业版", 1, "标准版", 2);
        Map<String, CvOverviewVo.Series> seriesByName = editions.stream()
            .collect(Collectors.toMap(Function.identity(), name -> series(name, monthStarts.size())));

        Date begin = monthStarts.get(0);
        Date end = monthStart(daysAfter(monthStarts.get(monthStarts.size() - 1), 32));
        List<CvLicenseIssue> issues = licenseIssueMapper.selectList(Wrappers.<CvLicenseIssue>lambdaQuery()
            .ne(CvLicenseIssue::getIssueStatus, ISSUE_STATUS_REVOKED)
            .ge(CvLicenseIssue::getIssuedTime, begin)
            .lt(CvLicenseIssue::getIssuedTime, end));

        for (CvLicenseIssue issue : issues) {
            String name = editionNames.getOrDefault(normalizeStatus(issue.getEdition()), StringUtils.blankToDefault(issue.getEdition(), "其他"));
            CvOverviewVo.Series series = seriesByName.computeIfAbsent(name, key -> series(key, monthStarts.size()));
            int index = monthIndex(monthStarts, issue.getIssuedTime());
            if (index >= 0) {
                series.getValues().set(index, series.getValues().get(index) + 1);
            }
        }

        CvOverviewVo.AuthorizationChart chart = new CvOverviewVo.AuthorizationChart();
        chart.setMonths(months);
        chart.setSeries(seriesByName.values().stream()
            .sorted((left, right) -> Integer.compare(
                editionOrder.getOrDefault(left.getName(), Integer.MAX_VALUE),
                editionOrder.getOrDefault(right.getName(), Integer.MAX_VALUE)))
            .toList());
        return chart;
    }

    private List<CvOverviewVo.Reminder> buildReminders(Date now, Date inThirtyDays) {
        List<CvOverviewVo.Reminder> reminders = new ArrayList<>();
        Map<Long, CvCustomer> customers = customersById();

        List<CvLicenseIssue> expiringIssues = licenseIssueMapper.selectList(Wrappers.<CvLicenseIssue>lambdaQuery()
            .ne(CvLicenseIssue::getIssueStatus, ISSUE_STATUS_REVOKED)
            .gt(CvLicenseIssue::getValidTo, now)
            .le(CvLicenseIssue::getValidTo, inThirtyDays)
            .orderByAsc(CvLicenseIssue::getValidTo)
            .last("limit 3"));
        for (CvLicenseIssue issue : expiringIssues) {
            String customerName = customerName(customers, issue.getCustomerId());
            long days = daysBetween(now, issue.getValidTo());
            reminders.add(reminder(
                customerName + "授权 " + days + " 天后到期",
                "建议跟进续签报价与联系人确认。"
            ));
        }

        List<CvReportTemplateDownloadToken> pendingTokens = downloadTokenMapper.selectList(Wrappers.<CvReportTemplateDownloadToken>lambdaQuery()
            .eq(CvReportTemplateDownloadToken::getTokenStatus, TOKEN_STATUS_ISSUED)
            .isNull(CvReportTemplateDownloadToken::getConsumedTime)
            .orderByDesc(CvReportTemplateDownloadToken::getCreateTime)
            .last("limit 3"));
        for (CvReportTemplateDownloadToken token : pendingTokens) {
            String customerName = customerName(customers, token.getCustomerId());
            reminders.add(reminder(
                customerName + "模板分发待确认",
                StringUtils.blankToDefault(token.getFileName(), "模板文件") + " 已推送，等待企业端下载确认。"
            ));
        }
        return reminders;
    }

    private List<CvOverviewVo.Todo> buildTodos(Date now, Date inThirtyDays) {
        List<CvOverviewVo.Todo> todos = new ArrayList<>();
        Map<Long, CvCustomer> customers = customersById();

        List<CvLicenseIssue> expiringIssues = licenseIssueMapper.selectList(Wrappers.<CvLicenseIssue>lambdaQuery()
            .ne(CvLicenseIssue::getIssueStatus, ISSUE_STATUS_REVOKED)
            .gt(CvLicenseIssue::getValidTo, now)
            .le(CvLicenseIssue::getValidTo, inThirtyDays)
            .orderByAsc(CvLicenseIssue::getValidTo)
            .last("limit 3"));
        for (CvLicenseIssue issue : expiringIssues) {
            todos.add(todo(
                "续费",
                customerName(customers, issue.getCustomerId()),
                "授权 " + daysBetween(now, issue.getValidTo()) + " 天后到期",
                "续签",
                "/vendor/renewal-order"
            ));
        }

        List<CvRenewalOrder> pendingOrders = renewalOrderMapper.selectList(Wrappers.<CvRenewalOrder>lambdaQuery()
            .eq(CvRenewalOrder::getOrderStatus, ORDER_STATUS_PENDING)
            .orderByDesc(CvRenewalOrder::getCreateTime)
            .last("limit 3"));
        for (CvRenewalOrder order : pendingOrders) {
            todos.add(todo(
                "续费",
                customerName(customers, order.getCustomerId()),
                "续费订单 " + order.getOrderNo() + " 待处理",
                "处理",
                "/vendor/renewal-order"
            ));
        }

        List<CvReportTemplateDownloadToken> pendingTokens = downloadTokenMapper.selectList(Wrappers.<CvReportTemplateDownloadToken>lambdaQuery()
            .eq(CvReportTemplateDownloadToken::getTokenStatus, TOKEN_STATUS_ISSUED)
            .isNull(CvReportTemplateDownloadToken::getConsumedTime)
            .orderByDesc(CvReportTemplateDownloadToken::getCreateTime)
            .last("limit 3"));
        for (CvReportTemplateDownloadToken token : pendingTokens) {
            todos.add(todo(
                "模板",
                customerName(customers, token.getCustomerId()),
                StringUtils.blankToDefault(token.getFileName(), "模板文件") + " 已分发待确认",
                "查看",
                "/vendor/template-scope"
            ));
        }
        return todos;
    }

    private long countActiveCustomers() {
        return customerMapper.selectCount(Wrappers.<CvCustomer>lambdaQuery()
            .notIn(CvCustomer::getCustomerStatus, List.of("disabled", "inactive", "stopped", "suspended", "1")));
    }

    private long countCustomersByStatus(String status) {
        return customerMapper.selectCount(Wrappers.<CvCustomer>lambdaQuery()
            .eq(CvCustomer::getCustomerStatus, status));
    }

    private long countIssuedLicenses(LambdaQueryWrapper<CvLicenseIssue> query) {
        query.ne(CvLicenseIssue::getIssueStatus, ISSUE_STATUS_REVOKED);
        return licenseIssueMapper.selectCount(query);
    }

    private Map<Long, CvCustomer> customersById() {
        return customerMapper.selectList(Wrappers.lambdaQuery()).stream()
            .filter(customer -> customer.getId() != null)
            .collect(Collectors.toMap(CvCustomer::getId, Function.identity(), (left, right) -> left));
    }

    private CvOverviewVo.Metric metric(String label, Long value, String note) {
        CvOverviewVo.Metric metric = new CvOverviewVo.Metric();
        metric.setLabel(label);
        metric.setValue(value);
        metric.setNote(note);
        return metric;
    }

    private CvOverviewVo.Series series(String name, int monthCount) {
        CvOverviewVo.Series series = new CvOverviewVo.Series();
        series.setName(name);
        series.setValues(new ArrayList<>());
        for (int i = 0; i < monthCount; i++) {
            series.getValues().add(0L);
        }
        return series;
    }

    private CvOverviewVo.Reminder reminder(String title, String description) {
        CvOverviewVo.Reminder reminder = new CvOverviewVo.Reminder();
        reminder.setTitle(title);
        reminder.setDescription(description);
        return reminder;
    }

    private CvOverviewVo.Todo todo(String type, String customer, String description, String action, String path) {
        CvOverviewVo.Todo todo = new CvOverviewVo.Todo();
        todo.setType(type);
        todo.setCustomer(customer);
        todo.setDescription(description);
        todo.setAction(action);
        todo.setPath(path);
        return todo;
    }

    private String customerName(Map<Long, CvCustomer> customers, Long customerId) {
        CvCustomer customer = customers.get(customerId);
        if (customer == null) {
            return "客户 " + customerId;
        }
        return StringUtils.blankToDefault(customer.getCustomerName(), customer.getCustomerCode());
    }

    private String normalizeStatus(String status) {
        return StringUtils.isBlank(status) ? null : status.trim().toLowerCase(Locale.ROOT);
    }

    private Date daysAfter(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    private Date monthStart(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private List<Date> lastMonthStarts(Date now, int count) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(monthStart(now));
        calendar.add(Calendar.MONTH, -(count - 1));
        List<Date> starts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            starts.add(calendar.getTime());
            calendar.add(Calendar.MONTH, 1);
        }
        return starts;
    }

    private String formatMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return (calendar.get(Calendar.MONTH) + 1) + "月";
    }

    private int monthIndex(List<Date> monthStarts, Date issuedTime) {
        if (issuedTime == null) {
            return -1;
        }
        Date issuedMonthStart = monthStart(issuedTime);
        for (int i = 0; i < monthStarts.size(); i++) {
            if (Objects.equals(monthStarts.get(i), issuedMonthStart)) {
                return i;
            }
        }
        return -1;
    }

    private long daysBetween(Date from, Date to) {
        LocalDate fromDate = from.toInstant().atZone(SYSTEM_ZONE).toLocalDate();
        LocalDate toDate = to.toInstant().atZone(SYSTEM_ZONE).toLocalDate();
        return Math.max(0, ChronoUnit.DAYS.between(fromDate, toDate));
    }
}
