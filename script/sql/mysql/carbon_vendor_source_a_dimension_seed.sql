-- Source(A) vendor-owned master-data seed.
-- Boundary: vendor cloud only. Enterprise-local sheets 102/104/201/204/3/4/5/501-504 are intentionally excluded.

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM cv_report_content_catalog WHERE remark = 'source(A)';
DELETE FROM cv_electricity_factor WHERE remark = 'source(A)';
DELETE FROM cv_electricity_factor_version WHERE remark = 'source(A)';
DELETE FROM cv_electricity_factor_scope WHERE remark = 'source(A)';
DELETE FROM cv_greenhouse_gas WHERE remark = 'source(A)';
DELETE FROM cv_base_year WHERE remark = 'source(A)';
DELETE FROM cv_emission_source_category WHERE remark = 'source(A)';
DELETE FROM cv_admin_division WHERE remark = 'source(A)';
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO cv_admin_division
(division_code, division_name, parent_code, level_type, sort_order, status, remark)
VALUES
('110000', '北京市', NULL, 'province', 10, '0', 'source(A)'),
('120000', '天津市', NULL, 'province', 20, '0', 'source(A)'),
('130000', '河北省', NULL, 'province', 30, '0', 'source(A)'),
('140000', '山西省', NULL, 'province', 40, '0', 'source(A)'),
('150001', '蒙西', NULL, 'province', 50, '0', 'source(A)'),
('150002', '蒙东', NULL, 'province', 60, '0', 'source(A)'),
('210000', '辽宁省', NULL, 'province', 70, '0', 'source(A)'),
('220000', '吉林省', NULL, 'province', 80, '0', 'source(A)'),
('230000', '黑龙江省', NULL, 'province', 90, '0', 'source(A)'),
('310000', '上海市', NULL, 'province', 100, '0', 'source(A)'),
('320000', '江苏省', NULL, 'province', 110, '0', 'source(A)'),
('330000', '浙江省', NULL, 'province', 120, '0', 'source(A)'),
('340000', '安徽省', NULL, 'province', 130, '0', 'source(A)'),
('350000', '福建省', NULL, 'province', 140, '0', 'source(A)'),
('360000', '江西省', NULL, 'province', 150, '0', 'source(A)'),
('370000', '山东省', NULL, 'province', 160, '0', 'source(A)'),
('410000', '河南省', NULL, 'province', 170, '0', 'source(A)'),
('420000', '湖北省', NULL, 'province', 180, '0', 'source(A)'),
('430000', '湖南省', NULL, 'province', 190, '0', 'source(A)'),
('440000', '广东省', NULL, 'province', 200, '0', 'source(A)'),
('450000', '广西壮族自治区', NULL, 'province', 210, '0', 'source(A)'),
('460000', '海南省', NULL, 'province', 220, '0', 'source(A)'),
('500000', '重庆市', NULL, 'province', 230, '0', 'source(A)'),
('510000', '四川省', NULL, 'province', 240, '0', 'source(A)'),
('520000', '贵州省', NULL, 'province', 250, '0', 'source(A)'),
('530000', '云南省', NULL, 'province', 260, '0', 'source(A)'),
('610000', '陕西省', NULL, 'province', 270, '0', 'source(A)'),
('620000', '甘肃省', NULL, 'province', 280, '0', 'source(A)'),
('630000', '青海省', NULL, 'province', 290, '0', 'source(A)'),
('640000', '宁夏回族自治区', NULL, 'province', 300, '0', 'source(A)'),
('650000', '新疆维吾尔自治区', NULL, 'province', 310, '0', 'source(A)')
ON DUPLICATE KEY UPDATE
    division_name = VALUES(division_name),
    parent_code = VALUES(parent_code),
    level_type = VALUES(level_type),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    remark = VALUES(remark);

INSERT INTO cv_emission_source_category
(category_code, business_key, category_name, category_name_en, ghg_scope, ghg_scope_category, iso_category, iso_category_en, iso_category_description, gb_scope_category, gb_subcategory, parent_code, effective_date, expire_date, current_flag, version_no, standard_category, sort_order, status, remark)
VALUES
('1', '101', '1.1 固定源燃烧', '1.1 Stationary Combustion', '范围1', '1.1 固定源燃烧', '类别1', 'Category 1', '直接温室气体排放', '1直接排放', '化石燃料燃烧排放-固定燃烧源', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U1-固定燃烧', 10, '0', 'source(A)'),
('2', '102', '1.2 移动源燃烧', '1.2 Mobile Combustion', '范围1', '1.2 移动源燃烧', '类别1', 'Category 1', '直接温室气体排放', '1直接排放', '化石燃料燃烧排放-移动燃烧源', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U1-移动燃烧', 20, '0', 'source(A)'),
('3', '103', '1.3 工艺排放', '1.3 Process Emissions', '范围1', '1.3 工艺排放', '类别1', 'Category 1', '直接温室气体排放', '1直接排放', '过程排放-生产过程排放', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U1-工艺排放', 30, '0', 'source(A)'),
('4', '104', '1.4 无组织逸散排放', '1.4 Fugitive Emissions', '范围1', '1.4 无组织逸散排放', '类别1', 'Category 1', '直接温室气体排放', '1直接排放', '过程排放-逸散排放', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U1-逸散排放', 40, '0', 'source(A)'),
('5', '105', '1.5 温室气体清除', '1.5 Carbon Removals', '范围1', '1.5 温室气体清除', '类别1', 'Category 1', '直接温室气体排放', '1直接排放', '温室气体清除', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U1-碳清除', 50, '0', 'source(A)'),
('6', '201', '2.1 外购电力', '2.1 Purchased Electricity', '范围2', '2.1 外购电力', '类别2', 'Category 2', '输入能源的间接温室气体排放', '2能源间接排放', '购入的电力产生的排放', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U2-外购电力', 60, '0', 'source(A)'),
('7', '202', '2.2 外购蒸汽', '2.2 Purchased Steam', '范围2', '2.2 外购蒸汽', '类别2', 'Category 2', '输入能源的间接温室气体排放', '2能源间接排放', '购入的热力产生的排放', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U2-外购蒸汽', 70, '0', 'source(A)'),
('8', '203', '2.3 外购热力', '2.3 Purchased Heat', '范围2', '2.3 外购热力', '类别2', 'Category 2', '输入能源的间接温室气体排放', '2能源间接排放', '购入的热力产生的排放', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U2-外购热力', 80, '0', 'source(A)'),
('9', '204', '2.4 外购冷量', '2.4 Purchased Cooling', '范围2', '2.4 外购冷量', '类别2', 'Category 2', '输入能源的间接温室气体排放', '2能源间接排放', '购入的热力产生的排放', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U2-外购冷量', 90, '0', 'source(A)'),
('10', '301', '3.1 采购的商品和服务', 'Category 1: Purchased Goods and Services', '范围3', '3.1 采购的商品和服务', '类别4', 'Category 4', '组织使用的产品或服务产生的间接温室气体排放', '3其他间接排放', '所使用的产品和服务隐含的排放', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U3-采购商品服务', 100, '0', 'source(A)'),
('11', '302', '3.2 资本货物', 'Category 2: Capital Goods', '范围3', '3.2 资本货物', '类别4', 'Category 4', '组织使用的产品或服务产生的间接温室气体排放', '3其他间接排放', '所使用的产品和服务隐含的排放', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U3-资本货物', 110, '0', 'source(A)'),
('12', '303', '3.3 燃料和能源相关活动', 'Category 3: Fuel and Energy Related Activities', '范围3', '3.3 燃料和能源相关活动', '类别4', 'Category 4', '组织使用的产品或服务产生的间接温室气体排放', '3其他间接排放', '所使用的产品和服务隐含的排放', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U3-燃料能源活动', 120, '0', 'source(A)'),
('13', '304', '3.4 上游运输和配送', 'Category 4: Upstream Transportation and Distribution', '范围3', '3.4 上游运输和配送', '类别3', 'Category 3', '运输产生的间接温室气体排放', '3其他间接排放', '交通运输产生排放', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U3-上游运输配送', 130, '0', 'source(A)'),
('14', '305', '3.5 运营中产生的废物', 'Category 5: Waste Generated in Operations', '范围3', '3.5 运营中产生的废物', '类别6', 'Category 6', '其他间接温室气体排放', '3其他间接排放', '废弃物处理处置排放', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U3-运营废物', 140, '0', 'source(A)'),
('15', '306', '3.6 商务旅行', 'Category 6: Business Travel', '范围3', '3.6 商务旅行', '类别3', 'Category 3', '运输产生的间接温室气体排放', '3其他间接排放', '交通运输产生排放', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U3-商务旅行', 150, '0', 'source(A)'),
('16', '307', '3.7 员工通勤', 'Category 7: Employee Commuting', '范围3', '3.7 员工通勤', '类别3', 'Category 3', '运输产生的间接温室气体排放', '3其他间接排放', '交通运输产生排放', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U3-员工通勤', 160, '0', 'source(A)'),
('17', '308', '3.8 上游租赁资产', 'Category 8: Upstream Leased Assets', '范围3', '3.8 上游租赁资产', '类别6', 'Category 6', '其他间接温室气体排放', '3其他间接排放', '所使用的产品和服务隐含的排放', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U3-上游租赁', 170, '0', 'source(A)'),
('18', '309', '3.9 下游运输和配送', 'Category 9: Downstream Transportation and Distribution', '范围3', '3.9 下游运输和配送', '类别3', 'Category 3', '运输产生的间接温室气体排放', '3其他间接排放', '交通运输产生排放', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U3-下游运输配送', 180, '0', 'source(A)'),
('19', '310', '3.10 销售产品的加工', 'Category 10: Processing of Sold Products', '范围3', '3.10 销售产品的加工', '类别5', 'Category 5', '与使用组织产品相关的间接温室气体排放', '3其他间接排放', '所生产的产品和服务的排放', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U3-产品加工', 190, '0', 'source(A)'),
('20', '311', '3.11 销售产品的使用', 'Category 11: Use of Sold Products', '范围3', '3.11 销售产品的使用', '类别5', 'Category 5', '与使用组织产品相关的间接温室气体排放', '3其他间接排放', '所生产的产品和服务的排放', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U3-产品使用', 200, '0', 'source(A)'),
('21', '312', '3.12 销售产品的报废处理', 'Category 12: End-of-Life Treatment of Sold Products', '范围3', '3.12 销售产品的报废处理', '类别5', 'Category 5', '与使用组织产品相关的间接温室气体排放', '3其他间接排放', '所生产的产品和服务的排放', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U3-产品报废', 210, '0', 'source(A)'),
('22', '313', '3.13 下游租赁资产', 'Category 13: Downstream Leased Assets', '范围3', '3.13 下游租赁资产', '类别6', 'Category 6', '其他间接温室气体排放', '3其他间接排放', '所生产的产品和服务的排放', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U3-下游租赁', 220, '0', 'source(A)'),
('23', '314', '3.14 特许经营', 'Category 14: Franchises', '范围3', '3.14 特许经营', '类别6', 'Category 6', '其他间接温室气体排放', '3其他间接排放', '所生产的产品和服务的排放', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U3-特许经营', 230, '0', 'source(A)'),
('24', '315', '3.15 投资', 'Category 15: Investments', '范围3', '3.15 投资', '类别6', 'Category 6', '其他间接温室气体排放', '3其他间接排放', '其他间接排放', NULL, '2024-01-01', '9999-12-31', 'Y', '1', 'U3-投资', 240, '0', 'source(A)')
ON DUPLICATE KEY UPDATE
    business_key = VALUES(business_key),
    category_name = VALUES(category_name),
    category_name_en = VALUES(category_name_en),
    ghg_scope = VALUES(ghg_scope),
    ghg_scope_category = VALUES(ghg_scope_category),
    iso_category = VALUES(iso_category),
    iso_category_en = VALUES(iso_category_en),
    iso_category_description = VALUES(iso_category_description),
    gb_scope_category = VALUES(gb_scope_category),
    gb_subcategory = VALUES(gb_subcategory),
    parent_code = VALUES(parent_code),
    effective_date = VALUES(effective_date),
    expire_date = VALUES(expire_date),
    current_flag = VALUES(current_flag),
    version_no = VALUES(version_no),
    standard_category = VALUES(standard_category),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    remark = VALUES(remark);

INSERT INTO cv_base_year
(base_year_key, base_year, is_current, description, sort_order, status, remark)
VALUES
('1', 2023, 1, '国家 / ISSB / 双碳基准', 10, '0', 'source(A)'),
('2', 2024, 0, '备用', 20, '0', 'source(A)'),
('3', 2025, 0, '备用', 30, '0', 'source(A)'),
('4', 2026, 0, '备用', 40, '0', 'source(A)'),
('5', 2027, 0, '备用', 50, '0', 'source(A)'),
('6', 2028, 0, '备用', 60, '0', 'source(A)'),
('7', 2029, 0, '备用', 70, '0', 'source(A)'),
('8', 2030, 0, '备用', 80, '0', 'source(A)')
ON DUPLICATE KEY UPDATE
    base_year = VALUES(base_year),
    is_current = VALUES(is_current),
    description = VALUES(description),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    remark = VALUES(remark);

INSERT INTO cv_electricity_factor_version
(factor_version, effective_year, sort_order, status, remark)
VALUES
('2022', 2023, 10, '0', 'source(A)'),
('2022', 2024, 20, '0', 'source(A)'),
('2023', 2025, 30, '0', 'source(A)'),
('2023', 2026, 40, '0', 'source(A)'),
('2023', 2027, 50, '0', 'source(A)')
ON DUPLICATE KEY UPDATE
    factor_version = VALUES(factor_version),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    remark = VALUES(remark);

INSERT INTO cv_electricity_factor
(factor_version, division_code, division_name, region_name, province_factor, region_factor, national_factor, non_fossil_excluded_factor, national_fossil_power_factor, sort_order, status, remark)
VALUES
('2022', '110000', '北京市', '华北', 0.558, 0.6776, 0.5366, 0.5856, 0.8325, 10, '0', 'source(A)'),
('2022', '120000', '天津市', '华北', 0.7041, 0.6776, 0.5366, 0.5856, 0.8325, 20, '0', 'source(A)'),
('2022', '130000', '河北省', '华北', 0.7252, 0.6776, 0.5366, 0.5856, 0.8325, 30, '0', 'source(A)'),
('2022', '140000', '山西省', '华北', 0.7096, 0.6776, 0.5366, 0.5856, 0.8325, 40, '0', 'source(A)'),
('2022', '150001', '蒙西', '华北', 0.6849, 0.6776, 0.5366, 0.5856, 0.8325, 50, '0', 'source(A)'),
('2022', '150002', '蒙东', '东北', 0.6849, 0.5564, 0.5366, 0.5856, 0.8325, 60, '0', 'source(A)'),
('2022', '210000', '辽宁省', '东北', 0.5626, 0.5564, 0.5366, 0.5856, 0.8325, 70, '0', 'source(A)'),
('2022', '220000', '吉林省', '东北', 0.4932, 0.5564, 0.5366, 0.5856, 0.8325, 80, '0', 'source(A)'),
('2022', '230000', '黑龙江省', '东北', 0.5368, 0.5564, 0.5366, 0.5856, 0.8325, 90, '0', 'source(A)'),
('2022', '310000', '上海市', '华东', 0.5849, 0.5617, 0.5366, 0.5856, 0.8325, 100, '0', 'source(A)'),
('2022', '320000', '江苏省', '华东', 0.5978, 0.5617, 0.5366, 0.5856, 0.8325, 110, '0', 'source(A)'),
('2022', '330000', '浙江省', '华东', 0.5153, 0.5617, 0.5366, 0.5856, 0.8325, 120, '0', 'source(A)'),
('2022', '340000', '安徽省', '华东', 0.6782, 0.5617, 0.5366, 0.5856, 0.8325, 130, '0', 'source(A)'),
('2022', '350000', '福建省', '华东', 0.4092, 0.5617, 0.5366, 0.5856, 0.8325, 140, '0', 'source(A)'),
('2022', '360000', '江西省', '华中', 0.5752, 0.5395, 0.5366, 0.5856, 0.8325, 150, '0', 'source(A)'),
('2022', '370000', '山东省', '华北', 0.641, 0.6776, 0.5366, 0.5856, 0.8325, 160, '0', 'source(A)'),
('2022', '410000', '河南省', '华中', 0.6058, 0.5395, 0.5366, 0.5856, 0.8325, 170, '0', 'source(A)'),
('2022', '420000', '湖北省', '华中', 0.4364, 0.5395, 0.5366, 0.5856, 0.8325, 180, '0', 'source(A)'),
('2022', '430000', '湖南省', '华中', 0.49, 0.5395, 0.5366, 0.5856, 0.8325, 190, '0', 'source(A)'),
('2022', '440000', '广东省', '南方', 0.4403, 0.3869, 0.5366, 0.5856, 0.8325, 200, '0', 'source(A)'),
('2022', '450000', '广西壮族自治区', '南方', 0.4044, 0.3869, 0.5366, 0.5856, 0.8325, 210, '0', 'source(A)'),
('2022', '460000', '海南省', '南方', 0.4184, 0.3869, 0.5366, 0.5856, 0.8325, 220, '0', 'source(A)'),
('2022', '500000', '重庆市', '西南', 0.5227, 0.2268, 0.5366, 0.5856, 0.8325, 230, '0', 'source(A)'),
('2022', '510000', '四川省', '西南', 0.1404, 0.2268, 0.5366, 0.5856, 0.8325, 240, '0', 'source(A)'),
('2022', '520000', '贵州省', '南方', 0.4989, 0.3869, 0.5366, 0.5856, 0.8325, 250, '0', 'source(A)'),
('2022', '530000', '云南省', '南方', 0.1073, 0.3869, 0.5366, 0.5856, 0.8325, 260, '0', 'source(A)'),
('2022', '610000', '陕西省', '西北', 0.6558, 0.5857, 0.5366, 0.5856, 0.8325, 270, '0', 'source(A)'),
('2022', '620000', '甘肃省', '西北', 0.4772, 0.5857, 0.5366, 0.5856, 0.8325, 280, '0', 'source(A)'),
('2022', '630000', '青海省', '西北', 0.1567, 0.5857, 0.5366, 0.5856, 0.8325, 290, '0', 'source(A)'),
('2022', '640000', '宁夏回族自治区', '西北', 0.6423, 0.5857, 0.5366, 0.5856, 0.8325, 300, '0', 'source(A)'),
('2022', '650000', '新疆维吾尔自治区', '西北', 0.6231, 0.5857, 0.5366, 0.5856, 0.8325, 310, '0', 'source(A)'),
('2023', '110000', '北京市', '华北', 0.5554, 0.6361, 0.5306, 0.6096, 0.8273, 320, '0', 'source(A)'),
('2023', '120000', '天津市', '华北', 0.6796, 0.6361, 0.5306, 0.6096, 0.8273, 330, '0', 'source(A)'),
('2023', '130000', '河北省', '华北', 0.6516, 0.6361, 0.5306, 0.6096, 0.8273, 340, '0', 'source(A)'),
('2023', '140000', '山西省', '华北', 0.6634, 0.6361, 0.5306, 0.6096, 0.8273, 350, '0', 'source(A)'),
('2023', '150001', '蒙西', '华北', 0.6479, 0.6361, 0.5306, 0.6096, 0.8273, 360, '0', 'source(A)'),
('2023', '150002', '蒙东', '东北', 0.6479, 0.5122, 0.5306, 0.6096, 0.8273, 370, '0', 'source(A)'),
('2023', '210000', '辽宁省', '东北', 0.4878, 0.5122, 0.5306, 0.6096, 0.8273, 380, '0', 'source(A)'),
('2023', '220000', '吉林省', '东北', 0.4671, 0.5122, 0.5306, 0.6096, 0.8273, 390, '0', 'source(A)'),
('2023', '230000', '黑龙江省', '东北', 0.5229, 0.5122, 0.5306, 0.6096, 0.8273, 400, '0', 'source(A)'),
('2023', '310000', '上海市', '华东', 0.5737, 0.55, 0.5306, 0.6096, 0.8273, 410, '0', 'source(A)'),
('2023', '320000', '江苏省', '华东', 0.5827, 0.55, 0.5306, 0.6096, 0.8273, 420, '0', 'source(A)'),
('2023', '330000', '浙江省', '华东', 0.4974, 0.55, 0.5306, 0.6096, 0.8273, 430, '0', 'source(A)'),
('2023', '340000', '安徽省', '华东', 0.6553, 0.55, 0.5306, 0.6096, 0.8273, 440, '0', 'source(A)'),
('2023', '350000', '福建省', '华东', 0.4211, 0.55, 0.5306, 0.6096, 0.8273, 450, '0', 'source(A)'),
('2023', '360000', '江西省', '华中', 0.5836, 0.5271, 0.5306, 0.6096, 0.8273, 460, '0', 'source(A)'),
('2023', '370000', '山东省', '华北', 0.6191, 0.6361, 0.5306, 0.6096, 0.8273, 470, '0', 'source(A)'),
('2023', '410000', '河南省', '华中', 0.5897, 0.5271, 0.5306, 0.6096, 0.8273, 480, '0', 'source(A)'),
('2023', '420000', '湖北省', '华中', 0.4044, 0.5271, 0.5306, 0.6096, 0.8273, 490, '0', 'source(A)'),
('2023', '430000', '湖南省', '华中', 0.4976, 0.5271, 0.5306, 0.6096, 0.8273, 500, '0', 'source(A)'),
('2023', '440000', '广东省', '南方', 0.4419, 0.4042, 0.5306, 0.6096, 0.8273, 510, '0', 'source(A)'),
('2023', '450000', '广西壮族自治区', '南方', 0.4476, 0.4042, 0.5306, 0.6096, 0.8273, 520, '0', 'source(A)'),
('2023', '460000', '海南省', '南方', 0.3648, 0.4042, 0.5306, 0.6096, 0.8273, 530, '0', 'source(A)'),
('2023', '500000', '重庆市', '西南', 0.5581, 0.2472, 0.5306, 0.6096, 0.8273, 540, '0', 'source(A)'),
('2023', '510000', '四川省', '西南', 0.1564, 0.2472, 0.5306, 0.6096, 0.8273, 550, '0', 'source(A)'),
('2023', '520000', '贵州省', '南方', 0.5683, 0.4042, 0.5306, 0.6096, 0.8273, 560, '0', 'source(A)'),
('2023', '530000', '云南省', '南方', 0.1333, 0.4042, 0.5306, 0.6096, 0.8273, 570, '0', 'source(A)'),
('2023', '610000', '陕西省', '西北', 0.6335, 0.5543, 0.5306, 0.6096, 0.8273, 580, '0', 'source(A)'),
('2023', '620000', '甘肃省', '西北', 0.4471, 0.5543, 0.5306, 0.6096, 0.8273, 590, '0', 'source(A)'),
('2023', '630000', '青海省', '西北', 0.1796, 0.5543, 0.5306, 0.6096, 0.8273, 600, '0', 'source(A)'),
('2023', '640000', '宁夏回族自治区', '西北', 0.6187, 0.5543, 0.5306, 0.6096, 0.8273, 610, '0', 'source(A)'),
('2023', '650000', '新疆维吾尔自治区', '西北', 0.6021, 0.5543, 0.5306, 0.6096, 0.8273, 620, '0', 'source(A)')
ON DUPLICATE KEY UPDATE
    division_name = VALUES(division_name),
    region_name = VALUES(region_name),
    province_factor = VALUES(province_factor),
    region_factor = VALUES(region_factor),
    national_factor = VALUES(national_factor),
    non_fossil_excluded_factor = VALUES(non_fossil_excluded_factor),
    national_fossil_power_factor = VALUES(national_fossil_power_factor),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    remark = VALUES(remark);

INSERT INTO cv_electricity_factor_scope
(scope_key, scope_name, sort_order, status, remark)
VALUES
('1', '全国因子', 10, '0', 'source(A)'),
('2', '区域因子', 20, '0', 'source(A)'),
('3', '省级因子', 30, '0', 'source(A)')
ON DUPLICATE KEY UPDATE
    scope_name = VALUES(scope_name),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    remark = VALUES(remark);

INSERT INTO cv_greenhouse_gas
(gas_code, gas_name, gas_name_en, gwp_value, gwp_version, chemical_formula, sort_order, status, remark)
VALUES
('CO2', 'CO2', 'CO2', NULL, NULL, 'CO2', 1, '0', 'source(A)'),
('CH4', 'CH4', 'CH4', NULL, NULL, 'CH4', 2, '0', 'source(A)'),
('N2O', 'N2O', 'N2O', NULL, NULL, 'N2O', 3, '0', 'source(A)'),
('HFCs', 'HFCs', 'HFCs', NULL, NULL, 'HFCs', 4, '0', 'source(A)'),
('PFCs', 'PFCs', 'PFCs', NULL, NULL, 'PFCs', 5, '0', 'source(A)'),
('SF6', 'SF6', 'SF6', NULL, NULL, 'SF6', 6, '0', 'source(A)'),
('NF3', 'NF3', 'NF3', NULL, NULL, 'NF3', 7, '0', 'source(A)')
ON DUPLICATE KEY UPDATE
    gas_name = VALUES(gas_name),
    gas_name_en = VALUES(gas_name_en),
    gwp_value = VALUES(gwp_value),
    gwp_version = VALUES(gwp_version),
    chemical_formula = VALUES(chemical_formula),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    remark = VALUES(remark);

INSERT INTO cv_report_content_catalog
(catalog_no, catalog_name, subcatalog_no, subcatalog_name, chart_list, sort_order, status, remark)
VALUES
('1', '1 总览｜集团碳排放驾驶舱', '1', '1.1 排放总览', 'CO2e排放总量（按照GHG Protocol体系分类）
CO2e排放总量TOP10排放源
范围占比
范围二外购电力CO2e排放总量
CO2e排放总量（按照工厂分类）
CO2e排放总量（按照行业分类）', 10, '0', 'source(A)'),
('1', '1 总览｜集团碳排放驾驶舱', '2', '1.2 时间趋势', 'CO2e排放总量趋势分析（按照年份）
CO2e排放总量趋势分析（按照月份）
CO2e排放总量趋势分析（同比）
CO2e排放总量趋势分析（环比）', 20, '0', 'source(A)'),
('1', '1 总览｜集团碳排放驾驶舱', '3', '1.3 数据导出', 'CO2e排放总量（按照GHG Protocol体系分类）
CO2e排放总量（按照ISO 14064‑1体系分类）
CO2e排放总量（按照GB/T 32150‑2025体系分类）
范围二外购电力CO2e排放总量', 30, '0', 'source(A)'),
('2', '2 核算组织/边界', '4', '2.1 核算组织/边界', 'CO2e排放总量（按照GHG Protocol体系分类）', 40, '0', 'source(A)'),
('3', '3 排放源识别与标准体系', '5', '3.1 标准体系', '排放源分类体系', 50, '0', 'source(A)'),
('3', '3 排放源识别与标准体系', '6', '3.2 排放源识别', '排放源识别清单', 60, '0', 'source(A)'),
('4', '4 活动数据管理', '7', '4.1 活动数据汇总', '活动数据汇总值（通用）
活动数据汇总值（电力）
活动数据汇总值kwh（电力）趋势分析（按照年份）', 70, '0', 'source(A)'),
('5', '5 排放因子库', '8', '5.1 通用因子库', '通用排放因子表', 80, '0', 'source(A)'),
('5', '5 排放因子库', '9', '5.2 电力因子库', '电力排放因子表
年份和电力因子对应表', 90, '0', 'source(A)'),
('6', '6 排放量核算结果', '10', '6.1 排放量总表', 'CO2e排放总量（按照GHG Protocol体系分类）
CO2e排放总量（按照ISO 14064‑1体系分类）
CO2e排放总量（按照GB/T 32150‑2025体系分类）', 100, '0', 'source(A)'),
('6', '6 排放量核算结果', '11', '6.2 细分排放', 'CO2e排放总量（按照工厂分类）
CO2e排放总量（按照行业分类）
范围二外购电力CO2e排放总量', 110, '0', 'source(A)'),
('6', '6 排放量核算结果', '12', '6.3 基准年对比', '年度排放量 vs 基准年（绝对量）
相对基准年的增减量（Δ 排放）
相对基准年的减排率（%）
按照排放范围对比
报告年份 vs 基准年
同比 vs 基准年差值', 120, '0', 'source(A)'),
('6', '6 排放量核算结果', '13', '6.4 GHGP视图', 'CO2e排放总量（按照GHG Protocol体系分类）', 130, '0', 'source(A)'),
('6', '6 排放量核算结果', '14', '6.5 ISO视图', 'CO2e排放总量（按照ISO 14064‑1体系分类）', 140, '0', 'source(A)'),
('6', '6 排放量核算结果', '15', '6.6 国标视图', 'CO2e排放总量（按照GB/T 32150‑2025体系分类）', 150, '0', 'source(A)'),
('6', '6 排放量核算结果', '16', '6.7 温室气体构成', '温室气体排放量
温室气体排放趋势
温室气体排放量（换算成CO2）', 160, '0', 'source(A)'),
('7', '7 行业对比分析', '17', '7.1 工厂对比分析', '（预留页面）', 170, '0', 'source(A)'),
('7', '7 行业对比分析', '18', '7.2 行业对标分析', '（预留页面）', 180, '0', 'source(A)'),
('8', '8 碳排放强度管理', '19', '8.1 强度总览', '碳排放强度汇总表
碳排放强度执行情况（实际 vs 目标）
工厂碳排放强度单位表
碳排放强度趋势
碳排放强度 vs 基准年（绝对量）', 190, '0', 'source(A)'),
('8', '8 碳排放强度管理', '20', '8.2 强度对比', '工厂碳排放强度单位表
碳排放强度 vs 产量
碳排放强度vs基准年 差值
碳排放强度 vs CO2e排放量', 200, '0', 'source(A)'),
('8', '8 碳排放强度管理', '21', '8.3 行业强度对标', '（预留页面）', 210, '0', 'source(A)'),
('9', '9 数据验证', '22', '9.1 数据验证', '（数据准确性验证页面）', 220, '0', 'source(A)'),
('10', '10 模型与方法学说明', '23', '10.1 数据模型说明', '10.1.1 数据模型总体设计原则
10.1.2 事实表与维度表设计
10.1.3 统一口径与集中计算设计
10.1.4 数据追溯与分析一致性', 230, '0', 'source(A)'),
('10', '10 模型与方法学说明', '24', '10.2 方法学说明', '10.2.1 排放核算基本方法
10.2.2 多气体核算与 CO2e 折算
10.2.3 排放因子与口径控制方法
10.2.4 基准年与变化分析方法
10.2.5 碳排放强度计算方法', 240, '0', 'source(A)')
ON DUPLICATE KEY UPDATE
    catalog_no = VALUES(catalog_no),
    catalog_name = VALUES(catalog_name),
    subcatalog_name = VALUES(subcatalog_name),
    chart_list = VALUES(chart_list),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    remark = VALUES(remark);

SELECT 'cv_admin_division' AS table_name, COUNT(*) AS source_a_rows FROM cv_admin_division WHERE remark = 'source(A)'
UNION ALL SELECT 'cv_emission_source_category', COUNT(*) FROM cv_emission_source_category WHERE remark = 'source(A)'
UNION ALL SELECT 'cv_base_year', COUNT(*) FROM cv_base_year WHERE remark = 'source(A)'
UNION ALL SELECT 'cv_electricity_factor_version', COUNT(*) FROM cv_electricity_factor_version WHERE remark = 'source(A)'
UNION ALL SELECT 'cv_electricity_factor', COUNT(*) FROM cv_electricity_factor WHERE remark = 'source(A)'
UNION ALL SELECT 'cv_electricity_factor_scope', COUNT(*) FROM cv_electricity_factor_scope WHERE remark = 'source(A)'
UNION ALL SELECT 'cv_greenhouse_gas', COUNT(*) FROM cv_greenhouse_gas WHERE remark = 'source(A)'
UNION ALL SELECT 'cv_report_content_catalog', COUNT(*) FROM cv_report_content_catalog WHERE remark = 'source(A)';
