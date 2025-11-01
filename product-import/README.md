# Product Import Batch

商品マスタCSVインポートバッチ

## 概要

このプロジェクトは、Spring Batch と MyBatis を使用した商品マスタCSVインポートバッチアプリケーションです。REST API経由でバッチジョブを起動し、データベースからデータを読み込み、処理し、更新します。

## 技術スタック

- Java 21
- Spring Boot 3.4.x
- Spring Batch 5.x
- MyBatis 3.x
- H2 Database (開発・テスト用)
- PostgreSQL (本番用)
- Flyway (データベースマイグレーション)
- Spring Boot Actuator (監視・メトリクス)
- Lombok
- Maven

## 必須環境

- Java 21以上
- Maven 3.6以上
- Docker (PostgreSQL環境を使用する場合)

## プロジェクト構造

```
product-import/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/productimport/
│   │   │       ├── Application.java              # メインクラス
│   │   │       ├── config/                       # 設定クラス
│   │   │       │   ├── BatchConfig.java
│   │   │       │   ├── DatabaseConfig.java
│   │   │       │   ├── LoggingAspect.java        # AOPロギング
│   │   │       │   └── BatchJobLauncher.java
│   │   │       ├── controller/                   # REST API
│   │   │       │   └── BatchJobController.java
│   │   │       ├── job/sample/                   # バッチジョブ
│   │   │       │   ├── SampleJobConfig.java
│   │   │       │   ├── SampleItemReader.java
│   │   │       │   ├── SampleItemProcessor.java
│   │   │       │   └── SampleItemWriter.java
│   │   │       ├── mapper/                       # MyBatisマッパー
│   │   │       ├── entity/                       # エンティティ
│   │   │       ├── dto/                          # DTO
│   │   │       └── exception/                    # 例外クラス
│   │   └── resources/
│   │       ├── application.yml                   # 開発環境設定
│   │       ├── application-prod.yml              # 本番環境設定
│   │       ├── mapper/                           # MyBatis XMLマッパー
│   │       └── db/migration/                     # Flywayマイグレーション
│   └── test/                                     # テスト
└── pom.xml
```

## セットアップ

### 1. プロジェクトのビルド

```bash
cd product-import
mvn clean compile
```

### 2. PostgreSQL環境の起動 (オプション)

```bash
docker-compose up -d
```

PostgreSQLが起動したら、以下のコマンドで確認できます:

```bash
docker-compose ps
```

## 実行方法

### 開発環境での実行 (H2データベース)

```bash
mvn spring-boot:run
```

### 本番環境での実行 (PostgreSQL)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

または環境変数で設定:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/productdb
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## バッチジョブの起動

### REST API経由でジョブを起動

```bash
curl -X POST http://localhost:8080/api/batch/jobs/sampleJob \
  -H "Content-Type: application/json" \
  -d '{
    "jobParameters": {
      "inputFile": "sample.csv"
    }
  }'
```

### ジョブ実行結果の取得

```bash
curl http://localhost:8080/api/batch/executions/{executionId}
```

## テスト実行

```bash
mvn test
```

## 監視・ヘルスチェック

### Actuatorエンドポイント

- ヘルスチェック: http://localhost:8080/actuator/health
- メトリクス: http://localhost:8080/actuator/metrics
- 情報: http://localhost:8080/actuator/info

### H2コンソール (開発環境のみ)

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:batchdb`
- Username: `sa`
- Password: (空欄)

## 主要機能

### AOPロギング

全てのpublicメソッドの開始・終了がINFOレベルでログ出力されます。実行時間も記録されます。

### Flywayマイグレーション

データベーススキーマは自動的にマイグレーションされます:
- V1: アプリケーションテーブル作成
- V2: Spring Batchメタデータテーブル作成

### サンプルジョブ

`sampleJob`は以下の処理を実行します:

#### 処理フロー

1. **Reader (SampleItemReader)**
   - MyBatisを使って`sample_entity`テーブルから10件ずつデータを読み込み
   - Flywayで自動投入された5件のサンプルデータが処理対象

2. **Processor (SampleItemProcessor)**
   - 名前に"INVALID"が含まれるデータをスキップ
   - ステータスを"PROCESSED"に更新
   - `updatedAt`を現在時刻に更新

3. **Writer (SampleItemWriter)**
   - MyBatisを使って処理済みデータをDBに更新

#### サンプルジョブの実行方法

1. アプリケーションを起動:
   ```bash
   mvn spring-boot:run
   ```

2. 別のターミナルでジョブを実行:
   ```bash
   curl -X POST http://localhost:8080/api/batch/jobs/sampleJob \
     -H "Content-Type: application/json" \
     -d '{"jobParameters": {}}'
   ```

3. 実行結果の確認:
   ```json
   {
     "executionId": 1,
     "status": "COMPLETED",
     "startTime": "2025-11-01T17:10:00",
     "endTime": "2025-11-01T17:10:01",
     "exitCode": "COMPLETED"
   }
   ```

#### データの確認

**H2コンソールで確認:**
1. ブラウザで http://localhost:8080/h2-console を開く
2. 接続情報を入力:
   - JDBC URL: `jdbc:h2:mem:batchdb`
   - User Name: `sa`
   - Password: (空欄)
3. SQLを実行:
   ```sql
   SELECT * FROM sample_entity;
   ```
   処理前: `status = 'NEW'`
   処理後: `status = 'PROCESSED'`

**ジョブ実行詳細を確認:**
```bash
curl http://localhost:8080/api/batch/executions/1
```

## カスタマイズ

### 新しいバッチジョブの追加

1. `job`パッケージに新しいジョブ設定クラスを作成
2. Reader, Processor, Writerを実装
3. 必要に応じてエンティティとマッパーを追加
4. Flywayマイグレーションでテーブルを作成

## トラブルシューティング

### ビルドエラー

```bash
mvn clean install -U
```

### データベース接続エラー

PostgreSQLが起動していることを確認:

```bash
docker-compose ps
docker-compose logs postgres
```

## ライセンス

MIT License
