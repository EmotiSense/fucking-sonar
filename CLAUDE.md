# CLAUDE.md — 工作约定

## 成功标准（核心）

**综合评分 ≥ 90 分是项目是否满足需求的唯一判定标准。**

评分依据来自论文 `article/2307.12082v4.pdf`（arXiv:2307.12082v4），该论文提出了一套基于 SonarQube 指标的软件代码质量量化模型。论文将质量分为三个一级维度，每项 SonarQube 指标用指数分布（单调指标）或非对称高斯分布（非单调指标）映射为 0–1 分，三维度加权后乘以 100 得到综合分。

### 论文三维度与 SonarQube 指标对照

| 维度 | SonarQube 指标 | 评分方式 | 满分条件 |
|------|---------------|---------|---------|
| **可维护性** | `sqale_rating` | Rating A = 满分 | A |
| | `code_smells` | 单调递减，越少越高 | 0 |
| | `duplicated_lines_density` | 非对称高斯，峰值在 0–3% | < 3% |
| **可靠性** | `reliability_rating` | Rating A = 满分 | A |
| | `bugs` | 单调递减，越少越高 | 0 |
| | `coverage` | 非对称高斯，峰值在 80% | ≥ 80% |
| **安全性** | `security_rating` | Rating A = 满分 | A |
| | `vulnerabilities` | 单调递减，越少越高 | 0 |

### 合格线

| 指标 | 最低要求 | 理想目标 |
|------|---------|---------|
| 综合评分 | **≥ 90 / 100** | ≥ 95 |
| 三项 Rating | 全部 A | 全部 A |
| Bugs | 0 | 0 |
| Vulnerabilities | 0 | 0 |
| Code Smells | 0 | 0 |
| Coverage | ≥ 60% | ≥ 80% |
| Duplicated Lines | < 15% | < 3% |

> 每次提交代码或重构后，必须用 SonarQube 实测验证综合评分，不允许用主观判断代替。

## 验证方式（SonarQube 实测）

- 依据论文 `article/2307.12082v4.pdf` 定义的指标体系，综合评分 ≥ 90 是任务完成的唯一标准
- 验证工具：本机 Docker 运行的 SonarQube 10-community（`http://localhost:9000`）
- 每次任务结束必须实际执行 `mvn clean verify sonar:sonar` 并通过 API 拉取真实指标，不允许用主观推断代替
- Token 过期时自动用 curl 生成新 token，不打断用户

## 常用命令

```bash
# 启动 SonarQube（如果未运行）
docker compose up -d

# 生成 SonarQube token（token 过期时用）
curl -s -u admin:admin -X POST "http://localhost:9000/api/user_tokens/generate" \
  -d "name=scan-$(date +%s)"

# 构建 + 测试 + 分析（一步到位）
mvn clean verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=<TOKEN>

# 拉取核心指标
curl -s -u admin:admin \
  "http://localhost:9000/api/measures/component?component=com.bookvault:bookvault\
&metricKeys=bugs,vulnerabilities,code_smells,coverage,duplicated_lines_density,\
sqale_rating,reliability_rating,security_rating,ncloc"
```

## 报告输出

- 任务完成后输出正式报告，文件名格式：`report.YYYY-MM-DD_HH_MM.md`
- 报告必须包含：SonarQube 实测指标表、论文三维度（可维护性/可靠性/安全性）对照、综合得分估算

## 工作方式偏好

- **先做完，再汇报**：不要边做边问，任务结束后统一输出结果
- **指标要实测**：不接受"理论上应该是 A"的说法，必须跑完扫描、拿到 API 数据才算完成
- **Token 过期自己处理**：用 curl 向本机 SonarQube API 自动生成新 token，不要打断用户去手动操作
- **Python 环境**：安装路径 `C:\Python311`，未加入系统 PATH，使用时用完整路径 `C:\Python311\python.exe`

## 环境信息

- OS：Windows 10，Shell：bash（用 Unix 语法）
- Java：17，Maven wrapper 可用
- SonarQube：Docker，`localhost:9000`，默认账号 `admin/admin`
- 项目 artifact：`com.bookvault:bookvault`
