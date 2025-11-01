# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## プロジェクト概要

本リポジトリは[Fintan Claude Code開発ガイド](https://fintan-contents.github.io/gai-dev-guide/claude-code-guide/introduction/)の学習用プロジェクトです。2つの独立したSpring Bootアプリケーションを含んでいます。

## プロジェクト構成

```
fintan-claude-code-guide/
├── product-import/     # Spring Batchバッチアプリケーション
├── task-api/           # REST APIアプリケーション
└── tmp/                # 一時ファイル保存用
```

各サブプロジェクトには独自の`CLAUDE.md`が存在します。サブプロジェクト内で作業する際は、そちらのガイドを参照してください。

## 必須環境

- Java 21
- Apache Maven 3.6以上
- Docker（PostgreSQL使用時）

## ビルドとテスト

各アプリケーションは独立してビルド・実行します。

### product-import

```bash
cd product-import
mvn clean compile
mvn spring-boot:run
mvn test
```

### task-api

```bash
cd task-api
export JAVA_HOME="$HOME/.asdf/installs/java/corretto-21.0.9.10.1"
export PATH="$JAVA_HOME/bin:$PATH"
mvn clean compile
mvn spring-boot:run
mvn test
```

## アーキテクチャ

### product-import: Spring Batchアプリケーション

**技術スタック**: Spring Boot 3.4、Spring Batch 5、MyBatis 3、H2/PostgreSQL、Flyway

**主要な設計判断**:

1. **REST APIでバッチ起動**: `BatchJobController`経由で`JobLauncher`を呼び出す
2. **MyBatisによるデータアクセス**: JPAではなくMyBatisを使用。マッパーインターフェースとXMLで実装
3. **チャンク指向処理**: Reader → Processor → Writerパターン（チャンクサイズ: 100件）
4. **スキップポリシー**: バリデーションエラーは処理を継続し、エラーファイルに出力
5. **AOPロギング**: `LoggingAspect`ですべてのpublicメソッドの開始・終了をログ出力

**バッチジョブの構造**:

```
Job (productImportJob)
  └─ Step (productImportStep)
       ├─ Reader: ProductCsvItemReader (CSVファイル読み込み)
       ├─ Processor: ProductItemProcessor (バリデーション)
       └─ Writer: ProductItemWriter (UPSERT処理)
```

**重要な設定**:
- `spring.batch.job.enabled: false` - アプリ起動時の自動実行を無効化
- `mybatis.configuration.map-underscore-to-camel-case: true` - snake_case ↔ camelCase自動変換
- Flywayによる自動マイグレーション（`db/migration/V*.sql`）

**バリデーション処理**:
- 6段階のバリデーション（必須、データ型、文字列長、範囲、論理、参照整合性）
- エラーはスキップしてCSVファイルに出力（`ErrorFileSkipListener`）
- 処理統計は`ProductImportJobExecutionListener`でログ出力

**Spring Batchメタデータ**:
- 6つのメタデータテーブルでジョブ実行履歴を管理
- 詳細は`product-import/SPRING_BATCH_STUDY.md`を参照

### task-api: REST APIアプリケーション

**技術スタック**: Spring Boot 3.4、MyBatis 3、H2、Flyway、JaCoCo

**主要な設計判断**:

1. **レイヤー構成**: Controller → Service → Mapper（repositoryではない）
2. **MyBatis使用**: SQLはXML (`src/main/resources/mapper/*.xml`)に記述
3. **バリデーション**: Jakarta Validationをエンティティに適用
4. **タイムスタンプ管理**: `createdAt`と`updatedAt`はServiceレイヤーで設定（DBデフォルト値を使用しない）
5. **例外処理**: `GlobalExceptionHandler`で集約的に処理

**パッケージ構成**:
```
com.example.taskapi
├── controller/     # REST APIエンドポイント
├── service/        # ビジネスロジック
├── mapper/         # MyBatisマッパーインターフェース
├── entity/         # エンティティ（Jakarta Validation付き）
├── dto/            # データ転送オブジェクト
└── exception/      # 例外クラスとハンドラー
```

**テスト戦略**:
- Mapperテスト: `@MybatisTest` + `@Sql` + Flyway無効化
- Serviceテスト: `@ExtendWith(MockitoExtension.class)` + Mapperモック
- JaCoCo: 行カバレッジ50%を要求（`mvn test`で自動チェック）

**重要な設定**:
- H2はインメモリモード (`jdbc:h2:mem:taskdb`)
- `ON UPDATE CURRENT_TIMESTAMP`は使用不可（H2非対応）
- Mapperテストでは`spring.flyway.enabled=false`を設定（競合回避）

## 共通の開発規約

### MyBatisの使用

両アプリケーションともMyBatis 3を採用しています。

**重要事項**:
- パッケージ名は`mapper`（`repository`ではない）
- SQLはXMLファイルに記述（アノテーションベースは使用しない）
- `map-underscore-to-camel-case: true`でDB列名とJavaプロパティを自動変換
- マッパーインターフェースは`@Mapper`アノテーション不要（`mybatis-spring-boot-starter`が自動検出）

### Flywayマイグレーション

**ルール**:
- マイグレーションファイル: `src/main/resources/db/migration/V{番号}__{説明}.sql`
- テスト用マイグレーション: `src/test/resources/db/migration/`に配置可能
- バージョン番号の欠番は許容されない
- 一度適用したマイグレーションは変更不可

### データベース

**開発環境**: H2 (インメモリ)
- product-import: `jdbc:h2:mem:batchdb`
- task-api: `jdbc:h2:mem:taskdb`

**本番環境**: PostgreSQL (product-importのみ対応)
- `mvn spring-boot:run -Dspring-boot.run.profiles=prod`で起動
- 環境変数: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`

### ログ出力

- AOPロギング（product-importのみ）: すべてのpublicメソッドの開始・終了を記録
- ログレベル: INFO（本番）、DEBUG（MyBatis SQLログ）
- SLF4J + Logbackを使用

## テストコマンド

### 全テスト実行

```bash
# product-import
cd product-import && mvn test

# task-api
cd task-api && mvn test
```

### 特定のテストクラス実行

```bash
mvn test -Dtest=ProductMapperTest
mvn test -Dtest=TaskServiceTest
```

### 特定のテストメソッド実行

```bash
mvn test -Dtest=ProductMapperTest#testUpsert
mvn test -Dtest=TaskServiceTest#testCreateTask
```

### カバレッジレポート確認

```bash
# task-api（JaCoCo有効）
cd task-api
mvn test
open target/site/jacoco/index.html
```

## API実行例

### product-import

```bash
# アプリ起動
cd product-import
mvn spring-boot:run

# バッチジョブ起動
curl -X POST http://localhost:8080/api/batch/jobs/productImportJob \
  -H "Content-Type: application/json" \
  -d '{"jobParameters": {"inputFilePath": "/path/to/products.csv"}}'

# ジョブ実行結果取得
curl http://localhost:8080/api/batch/executions/1
```

### task-api

```bash
# アプリ起動
cd task-api
mvn spring-boot:run

# タスク一覧取得
curl http://localhost:8080/api/tasks

# タスク作成
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "新しいタスク", "description": "説明", "completed": false}'
```

## 参考資料

プロジェクト固有の詳細情報は各ディレクトリのドキュメントを参照してください。

- [product-import/README.md](./product-import/README.md) - バッチアプリの詳細仕様
- [product-import/SPRING_BATCH_STUDY.md](./product-import/SPRING_BATCH_STUDY.md) - Spring Batchメタデータ管理の解説
- [product-import/CLAUDE.md](./product-import/CLAUDE.md) - product-import用のClaude Codeガイド（存在する場合）
- [task-api/CLAUDE.md](./task-api/CLAUDE.md) - task-api用のClaude Codeガイド
- [task-api/API_TEST_GUIDE.md](./task-api/API_TEST_GUIDE.md) - API仕様とテストガイド
- [Fintan Claude Code開発ガイド](https://fintan-contents.github.io/gai-dev-guide/claude-code-guide/introduction/)
