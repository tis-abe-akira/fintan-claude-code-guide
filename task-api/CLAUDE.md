# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## プロジェクト概要

Spring Boot 3.4、MyBatis 3、H2データベースを使用したタスク管理REST APIです。

## 必須環境

- Java 21
- Apache Maven 3.9
- asdfでJavaバージョンを管理（`.tool-versions`にJava 21を設定済み）

## ビルドとテスト

### ビルド

```bash
export JAVA_HOME="$HOME/.asdf/installs/java/corretto-21.0.9.10.1"
export PATH="$JAVA_HOME/bin:$PATH"
mvn clean compile
```

### アプリケーション起動

```bash
mvn spring-boot:run
```

起動後、`http://localhost:8080`でアクセス可能。H2コンソールは`http://localhost:8080/h2-console`。

### テスト実行

```bash
# 全テスト実行
mvn test

# 特定のテストクラスを実行
mvn test -Dtest=TaskMapperTest
mvn test -Dtest=TaskServiceTest

# 特定のテストメソッドを実行
mvn test -Dtest=TaskMapperTest#testInsert
```

## アーキテクチャ

### レイヤー構成

レイヤー別パッケージ構成を採用：

```
com.example.taskapi
├── controller/     # REST APIエンドポイント
├── service/        # ビジネスロジック
├── mapper/         # MyBatisマッパーインターフェース（repositoryではない）
├── entity/         # エンティティ（Jakarta Validationアノテーション付き）
├── dto/            # データ転送オブジェクト
└── exception/      # 例外クラスとグローバルハンドラー
```

### データアクセス層の特徴

- **MyBatis使用**: JPAではなくMyBatis 3を使用
- **SQLはXMLに記述**: `src/main/resources/mapper/*.xml`にSQL文を定義
- **Mapper命名**: `repository`パッケージではなく`mapper`パッケージ
- **スネークケース変換**: `map-underscore-to-camel-case=true`によりDB列名（snake_case）とJavaプロパティ（camelCase）を自動変換

### データベースマイグレーション

- **Flyway使用**: `src/main/resources/db/migration/V*__*.sql`
- **H2データベース**: インメモリモード（`jdbc:h2:mem:taskdb`）
- **重要**: H2は`ON UPDATE CURRENT_TIMESTAMP`をサポートしないため、テスト用SQLでは使用しない

### バリデーション

- Task entityに以下の制約あり:
  - `title`: 必須（`@NotBlank`）、最大100文字（`@Size`）
  - `description`: 任意、最大500文字（`@Size`）
  - `completed`: 必須（`@NotNull`）、デフォルト`false`
- バリデーションエラーは`GlobalExceptionHandler`で400エラーとして処理

### 例外処理

- `ResourceNotFoundException`: タスクが見つからない場合（404）
- `GlobalExceptionHandler`: `@RestControllerAdvice`で集約処理
  - `ResourceNotFoundException` → 404 Not Found
  - `MethodArgumentNotValidException` → 400 Bad Request
  - その他の例外 → 500 Internal Server Error

## テスト戦略

### Mapperテストのポイント

- `@MybatisTest`を使用
- `@TestPropertySource`で`spring.flyway.enabled=false`を設定（FlywayとSQLスクリプトの競合回避）
- `@Sql`でテスト用テーブル作成（`BEFORE_TEST_CLASS`）とデータクリーンアップ（`AFTER_TEST_METHOD`）
- テスト用マイグレーションは`src/test/resources/db/migration/`に配置

### Serviceテストのポイント

- `@ExtendWith(MockitoExtension.class)`でMapperをモック化
- `@Mock`と`@InjectMocks`を使用
- Mapperの動作をモックすることでServiceロジックのみをテスト

## API仕様

詳細は`API_TEST_GUIDE.md`を参照。

### エンドポイント

- `GET /api/tasks` - 全タスク取得
- `GET /api/tasks/{id}` - 特定タスク取得
- `POST /api/tasks` - タスク作成（201 Created）
- `PUT /api/tasks/{id}` - タスク更新
- `DELETE /api/tasks/{id}` - タスク削除（204 No Content）
- `PATCH /api/tasks/{id}/toggle` - 完了状態切り替え

## 重要な設計判断

### タイムスタンプの扱い

- `createdAt`と`updatedAt`はServiceレイヤーで設定（DBのデフォルト値を使用しない）
- 理由: ビジネスロジックとして明示的に制御するため

### Mapperの命名

- パッケージ名は`mapper`（`repository`ではない）
- XMLファイルも`mapper/`ディレクトリに配置
- MyBatis使用を明示するため

### completedフィールドのデフォルト値

- Entityクラスで`false`をデフォルト設定
- Service層でnullチェックして`false`に設定
- 二重チェックで確実性を担保
