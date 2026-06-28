package org.dromara.carbon.vendor.overview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.customer.domain.CvCustomer;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.renewal.domain.CvRenewalOrder;
import org.dromara.carbon.vendor.template.domain.CvReportTemplateDownloadToken;
import org.dromara.carbon.vendor.overview.domain.vo.CvOverviewVo;
import org.dromara.carbon.vendor.customer.mapper.CvCustomerMapper;
import org.dromara.carbon.vendor.license.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.renewal.mapper.CvRenewalOrderMapper;
import org.dromara.carbon.vendor.template.mapper.CvReportTemplateDownloadTokenMapper;
import org.dromara.carbon.vendor.overview.service.ICvOverviewService;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.system.domain.SysTenantPackage;
import org.dromara.system.mapper.SysTenantPackageMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    private final SysTenantPackageMapper tenantPackageMapper;

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
        Map<String, CvOverviewVo.Series> seriesByName = new LinkedHashMap<>();

        Date begin = monthStarts.get(0);
        Date end = monthStart(daysAfter(monthStarts.get(monthStarts.size() - 1), 32));
        List<CvLicenseIssue> issues = licenseIssueMapper.selectList(Wrappers.<CvLicenseIssue>lambdaQuery()
            .ne(CvLicenseIssue::getIssueStatus, ISSUE_STATUS_REVOKED)
            .ge(CvLicenseIssue::getIssuedTime, begin)
            .lt(CvLicenseIssue::getIssuedTime, end));
        Map<Long, String> packageNames = currentPackageNames(issues);

        for (CvLicenseIssue issue : issues) {
            String name = packageSeriesName(issue, packageNames);
            CvOverviewVo.Series series = seriesByName.computeIfAbsent(name, key -> series(key, monthStarts.size()));
            int index = monthIndex(monthStarts, issue.getIssuedTime());
            if (index >= 0) {
                series.getValues().set(index, series.getValues().get(index) + 1);
            }
        }

        CvOverviewVo.AuthorizationChart chart = new CvOverviewVo.AuthorizationChart();
        chart.setMonths(months);
        chart.setSeries(seriesByName.values().stream()
            .sorted(Comparator.comparing(CvOverviewVo.Series::getName, Comparator.nullsLast(String::compareTo)))
            .toList());
        return chart;
    }

    private String packageSeriesName(CvLicenseIssue issue, Map<Long, String> packageNames) {
        Long packageId = issue.getPackageId();
        if (packageId != null) {
            return StringUtils.blankToDefault(packageNames.get(packageId), unconfiguredPackageName(packageId));
        }
        return "未指定套餐";
    }

    private List<CvOverviewVo.Reminder> buildReminders(Date now, Date inThirtyDays) {
        List<CvOverviewVo.Reminder> reminders = new ArrayList<>();
        Set<String> reminderKeys = new HashSet<>();
        Map<Long, CvCustomer> customers = customersById();

        List<CvLicenseIssue> expiringIssues = expiringLicenseIssues(now, inThirtyDays, 3);
        for (CvLicenseIssue issue : expiringIssues) {
            String customerName = customerName(customers, issue.getCustomerId());
            long days = daysBetween(now, issue.getValidTo());
            addReminderIfAbsent(reminders, reminderKeys, "license-expiry:" + customerKey(issue.getCustomerId(), issue.getLicenseId()), reminder(
                customerName + "授权 " + days + " 天后到期",
                "建议跟进续签报价与联系人确认。"
            ));
        }

        List<CvReportTemplateDownloadToken> pendingTokens = pendingDownloadTokens(3);
        for (CvReportTemplateDownloadToken token : pendingTokens) {
            String customerName = customerName(customers, token.getCustomerId());
            addReminderIfAbsent(reminders, reminderKeys, "template-token:" + tokenBusinessKey(token), reminder(
                customerName + "模板分发待确认",
                StringUtils.blankToDefault(token.getFileName(), "模板文件") + " 已推送，等待企业端下载确认。"
            ));
        }
        return reminders;
    }

    private List<CvOverviewVo.Todo> buildTodos(Date now, Date inThirtyDays) {
        List<CvOverviewVo.Todo> todos = new ArrayList<>();
        Set<String> todoKeys = new HashSet<>();
        Map<Long, CvCustomer> customers = customersById();
        List<CvRenewalOrder> pendingOrders = pendingRenewalOrders();
        Set<Long> pendingRenewalCustomerIds = pendingOrders.stream()
            .map(CvRenewalOrder::getCustomerId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        List<CvLicenseIssue> expiringIssues = expiringLicenseIssues(now, inThirtyDays, 3).stream()
            .filter(issue -> issue.getCustomerId() == null || !pendingRenewalCustomerIds.contains(issue.getCustomerId()))
            .limit(3)
            .toList();
        for (CvLicenseIssue issue : expiringIssues) {
            addTodoIfAbsent(todos, todoKeys, "license-expiry:" + customerKey(issue.getCustomerId(), issue.getLicenseId()), todo(
                "续费",
                customerName(customers, issue.getCustomerId()),
                "授权 " + daysBetween(now, issue.getValidTo()) + " 天后到期",
                "续签",
                "/vendor/renewal-order"
            ));
        }

        for (CvRenewalOrder order : pendingOrders.stream().limit(3).toList()) {
            addTodoIfAbsent(todos, todoKeys, "renewal-order:" + renewalOrderKey(order), todo(
                "续费",
                customerName(customers, order.getCustomerId()),
                "续费订单 " + order.getOrderNo() + " 待处理",
                "处理",
                "/vendor/renewal-order"
            ));
        }

        List<CvReportTemplateDownloadToken> pendingTokens = pendingDownloadTokens(3);
        for (CvReportTemplateDownloadToken token : pendingTokens) {
            addTodoIfAbsent(todos, todoKeys, "template-token:" + tokenBusinessKey(token), todo(
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

    private Map<Long, String> currentPackageNames(List<CvLicenseIssue> issues) {
        Set<Long> packageIds = issues.stream()
            .map(CvLicenseIssue::getPackageId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        if (packageIds.isEmpty()) {
            return Map.of();
        }
        return TenantHelper.ignore(() -> tenantPackageMapper.selectBatchIds(packageIds)).stream()
            .filter(tenantPackage -> tenantPackage.getPackageId() != null)
            .filter(tenantPackage -> StringUtils.isNotBlank(tenantPackage.getPackageName()))
            .collect(Collectors.toMap(SysTenantPackage::getPackageId, SysTenantPackage::getPackageName, (left, right) -> left));
    }

    private List<CvLicenseIssue> expiringLicenseIssues(Date now, Date inThirtyDays, long size) {
        return distinctBy(licenseIssueMapper.selectList(Wrappers.<CvLicenseIssue>lambdaQuery()
                .ne(CvLicenseIssue::getIssueStatus, ISSUE_STATUS_REVOKED)
                .gt(CvLicenseIssue::getValidTo, now)
                .le(CvLicenseIssue::getValidTo, inThirtyDays)
                .orderByAsc(CvLicenseIssue::getValidTo)),
            issue -> customerKey(issue.getCustomerId(), issue.getLicenseId()))
            .stream()
            .limit(size)
            .toList();
    }

    private List<CvRenewalOrder> pendingRenewalOrders() {
        return distinctBy(renewalOrderMapper.selectList(Wrappers.<CvRenewalOrder>lambdaQuery()
                .eq(CvRenewalOrder::getOrderStatus, ORDER_STATUS_PENDING)
                .orderByDesc(CvRenewalOrder::getCreateTime)),
            this::renewalOrderKey);
    }

    private List<CvReportTemplateDownloadToken> pendingDownloadTokens(long size) {
        return distinctBy(downloadTokenMapper.selectList(Wrappers.<CvReportTemplateDownloadToken>lambdaQuery()
                .eq(CvReportTemplateDownloadToken::getTokenStatus, TOKEN_STATUS_ISSUED)
                .isNull(CvReportTemplateDownloadToken::getConsumedTime)
                .orderByDesc(CvReportTemplateDownloadToken::getCreateTime)),
            this::tokenBusinessKey)
            .stream()
            .limit(size)
            .toList();
    }

    private <T> List<T> distinctBy(List<T> items, Function<T, String> keyResolver) {
        Map<String, T> uniqueItems = new LinkedHashMap<>();
        for (T item : items) {
            uniqueItems.putIfAbsent(keyResolver.apply(item), item);
        }
        return new ArrayList<>(uniqueItems.values());
    }

    private void addReminderIfAbsent(List<CvOverviewVo.Reminder> reminders, Set<String> keys,
                                     String key, CvOverviewVo.Reminder reminder) {
        if (keys.add(key)) {
            reminders.add(reminder);
        }
    }

    private void addTodoIfAbsent(List<CvOverviewVo.Todo> todos, Set<String> keys, String key, CvOverviewVo.Todo todo) {
        if (keys.add(key)) {
            todos.add(todo);
        }
    }

    private String customerKey(Long customerId, String fallback) {
        return customerId == null ? StringUtils.blankToDefault(fallback, "unknown") : String.valueOf(customerId);
    }

    private String renewalOrderKey(CvRenewalOrder order) {
        return StringUtils.blankToDefault(order.getOrderNo(),
            customerKey(order.getCustomerId(), String.valueOf(order.getId())));
    }

    private String tokenBusinessKey(CvReportTemplateDownloadToken token) {
        return customerKey(token.getCustomerId(), token.getLicenseId())
            + ":" + Objects.toString(token.getTemplateId(), "")
            + ":" + StringUtils.blankToDefault(token.getFileName(), token.getDownloadToken());
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

    private String unconfiguredPackageName(Long packageId) {
        return "套餐未配置#" + packageId;
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
