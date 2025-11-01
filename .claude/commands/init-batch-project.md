# .claude/commands/init-batch-project.md
---
argument-hint: <プロジェクト名> <パッケージ名> [説明]
description: Spring Batch + MyBatis を使用した Spring Boot プロジェクトを初期化
model: sonnet
allowed-tools: Write, Bash
---

以下の仕様でSpring Batch + MyBatisプロジェクトを初期化してください：

## 基本要件
- プロジェクト名: $1
- ベースパッケージ名: $2
- プロジェクト説明: $3 (省略時は「Spring Batch Application」)

## 技術スタック
- Spring Boot 3.4.x
- Spring Batch 5.x
- MyBatis 3.x (mybatis-spring-boot-starter)
- H2 Database (開発・テスト用)
- PostgreSQL (本番用)
- Flyway (データベースマイグレーション)
- Spring Boot Actuator (監視・メトリクス)
- Lombok (ボイラープレートコード削減)
- JUnit 5 + Mockito (テスト)
- Java 21
- Maven

## 生成する成果物

### 1. プロジェクト構造

```
{project-name}/
├── pom.xml
├── .gitignore
├── README.md
├── docker-compose.yml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── {package-path}/
    │   │       ├── Application.java
    │   │       ├── config/
    │   │       │   ├── BatchConfig.java
    │   │       │   ├── DatabaseConfig.java
    │   │       │   ├── LoggingAspect.java
    │   │       │   └── BatchJobLauncher.java
    │   │       ├── controller/
    │   │       │   └── BatchJobController.java
    │   │       ├── job/
    │   │       │   └── sample/
    │   │       │       ├── SampleJobConfig.java
    │   │       │       ├── SampleItemReader.java
    │   │       │       ├── SampleItemProcessor.java
    │   │       │       └── SampleItemWriter.java
    │   │       ├── mapper/
    │   │       │   └── SampleMapper.java
    │   │       ├── entity/
    │   │       │   └── SampleEntity.java
    │   │       ├── dto/
    │   │       │   ├── JobExecutionRequest.java
    │   │       │   └── JobExecutionResponse.java
    │   │       └── exception/
    │   │           └── BatchJobException.java
    │   └── resources/
    │       ├── application.yml
    │       ├── application-prod.yml
    │       ├── application-test.yml
    │       ├── mapper/
    │       │   └── SampleMapper.xml
    │       └── db/
    │           └── migration/
    │               ├── V1__init_schema.sql
    │               └── V2__init_batch_tables.sql
    └── test/
        ├── java/
        │   └── {package-path}/
        │       ├── job/
        │       │   └── SampleJobConfigTest.java
        │       └── mapper/
        │           └── SampleMapperTest.java
        └── resources/
            └── application-test.yml
```

### 2. 各ファイルの仕様

#### pom.xml
- Spring Boot 3.4.x の依存関係
- 必須依存:
  - spring-boot-starter-batch
  - spring-boot-starter-web (REST API用)
  - mybatis-spring-boot-starter (3.0.x)
  - spring-boot-starter-actuator
  - spring-boot-starter-aop (ロギングAOP用)
  - flyway-core
  - h2 (runtimeScope)
  - postgresql (runtimeScope)
  - lombok (provided)
  - spring-boot-starter-test (test)
  - spring-batch-test (test)
  - mybatis-spring-boot-starter-test (test)

#### application.yml
開発環境設定（H2使用）:
- server.port: 8080
- spring.datasource: H2インメモリDB設定
- spring.batch.jdbc.initialize-schema: always
- mybatis.configuration:
  - map-underscore-to-camel-case: true
  - default-fetch-size: 100
- mybatis.mapper-locations: classpath:mapper/**/*.xml
- mybatis.type-aliases-package: {package}.entity
- spring.batch.job.enabled: false (REST API経由起動のため)
- flyway.enabled: true
- management.endpoints.web.exposure.include: health,info,metrics
- logging.level: INFO

#### application-prod.yml
本番環境設定（PostgreSQL使用）:
- spring.datasource: PostgreSQL接続設定（環境変数参照）
- spring.batch.jdbc.initialize-schema: never (Flywayが管理)
- flyway.enabled: true

#### application-test.yml
テスト環境設定（H2使用）:
- H2インメモリDB
- ログレベル: DEBUG

#### BatchConfig.java
- @Configuration, @EnableBatchProcessing
- JobRepository, JobLauncher, TransactionManagerのBean定義
- 共通的なバッチ設定

#### DatabaseConfig.java
- @Configuration
- DataSource設定（環境別）
- SqlSessionFactory設定
- TransactionManager設定
- @MapperScan設定

#### LoggingAspect.java
- @Aspect, @Component
- @Around("execution(public * {package}..*.*(..))") でpublicメソッドをインターセプト
- メソッド開始時: log.info("Starting method: {}", joinPoint.getSignature())
- メソッド終了時: log.info("Completed method: {}, Duration: {} ms", joinPoint.getSignature(), duration)
- 例外発生時: log.error("Exception in method: {}", joinPoint.getSignature(), exception)

#### BatchJobLauncher.java
- @Component
- JobLauncher, JobExplorer, JobRegistry をDI
- ジョブ実行ロジックを提供するサービスクラス
- ジョブパラメータ構築、実行、結果取得メソッド

#### BatchJobController.java
- @RestController, @RequestMapping("/api/batch")
- POST /api/batch/jobs/{jobName}: ジョブ起動
- GET /api/batch/jobs/{jobName}/executions: ジョブ実行履歴取得
- GET /api/batch/jobs/{executionId}: 実行詳細取得
- BatchJobLauncher を使用

#### SampleJobConfig.java
- @Configuration
- Jobの定義:
  - Job名: sampleJob
  - Step: sampleStep (chunk size: 10)
  - Reader → Processor → Writer
- ItemReader, ItemProcessor, ItemWriter の Bean定義

#### SampleItemReader.java
- MyBatisPagingItemReader または MyBatisCursorItemReader を使用
- SampleMapper.selectAll() を呼び出し

#### SampleItemProcessor.java
- ItemProcessor<SampleEntity, SampleEntity> 実装
- ビジネスロジック例（データ変換・検証）

#### SampleItemWriter.java
- ItemWriter<SampleEntity> 実装
- MyBatis Mapperを使用してDB更新
- またはログ出力などの処理

#### SampleMapper.java
- @Mapper インターフェース
- selectAll(): List<SampleEntity>
- insert(SampleEntity entity): int
- update(SampleEntity entity): int

#### SampleMapper.xml
- namespace: {package}.mapper.SampleMapper
- selectAll, insert, update のSQL定義
- resultMap定義

#### SampleEntity.java
- @Data, @NoArgsConstructor, @AllArgsConstructor (Lombok)
- フィールド: id (Long), name (String), status (String), createdAt (LocalDateTime), updatedAt (LocalDateTime)
- バリデーションアノテーション: @NotBlank, @Size など

#### JobExecutionRequest.java
- @Data
- フィールド: jobParameters (Map<String, Object>)

#### JobExecutionResponse.java
- @Data
- フィールド: executionId (Long), status (String), startTime (LocalDateTime), endTime (LocalDateTime), exitCode (String)

#### BatchJobException.java
- RuntimeException を継承
- バッチ処理固有の例外

#### V1__init_schema.sql
- sample_entity テーブル作成SQL
- 必要に応じてマスタテーブル作成

#### V2__init_batch_tables.sql
- Spring Batch メタデータテーブル作成SQL
- BATCH_JOB_INSTANCE, BATCH_JOB_EXECUTION, BATCH_STEP_EXECUTION など
- PostgreSQL用のDDL

#### docker-compose.yml
- PostgreSQL 16 コンテナ定義
- ポート: 5432
- 環境変数: POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD
- ボリューム: postgres-data

#### README.md
- プロジェクト概要
- 必須環境（Java 21, Maven）
- ビルド手順: `mvn clean compile`
- アプリケーション起動: `mvn spring-boot:run`
- テスト実行: `mvn test`
- バッチジョブ起動方法: REST API例
- Docker環境構築: `docker-compose up -d`
- Actuatorエンドポイント: /actuator/health

#### .gitignore
- target/
- *.class
- .idea/
- *.iml
- .vscode/
- .DS_Store
- application-local.yml

#### SampleJobConfigTest.java
- @SpringBatchTest, @SpringBootTest
- JobLauncherTestUtils, JobRepositoryTestUtils をDI
- testSampleJob(): ジョブ実行テスト、ステータス検証

#### SampleMapperTest.java
- @MybatisTest
- @AutoConfigureTestDatabase(replace = NONE)
- @Sql でテストデータ投入
- selectAll, insert, update のテスト

## コーディング規約
- Google Java Style Guide 準拠
- JavaDoc コメント必須（クラス、publicメソッド）
- Lombok で @Data, @Slf4j などを活用
- フィールドは private で定義
- REST APIは適切なHTTPステータスコードを返却

## 実装手順
1. プロジェクトディレクトリ作成
2. pom.xml 生成
3. ディレクトリ構造作成
4. application.yml ファイル群生成
5. Javaクラス生成（config, controller, job, mapper, entity, dto, exception）
6. MyBatis XMLマッパー生成
7. Flywayマイグレーションファイル生成
8. テストクラス生成
9. README.md, .gitignore, docker-compose.yml 生成
10. Maven依存関係解決: `mvn dependency:resolve`

生成後、ビルド可能な状態であることを確認してください。

## 使用例
```
/init-batch-project order-batch com.example.orderbatch "注文データバッチ処理システム"
```
