# Spring Batch メタデータ管理の仕組み

## 概要

Spring Batchは**6つの主要テーブル**でジョブとステップの実行履歴を管理しています。
これらのテーブルは、バッチ処理の実行状況追跡、リスタート機能、統計レポートなどを実現するための基盤です。

## メタデータテーブル構造

### 1. BATCH_JOB_INSTANCE - ジョブの定義

**役割**: 「どんなジョブが存在するか」を記録

```
JOB_INSTANCE_ID | JOB_NAME    | JOB_KEY
1               | sampleJob   | d41d8cd98f00b204e9800998ecf8427e
```

- **JOB_KEY**: ジョブパラメータのハッシュ値（同じパラメータなら同じインスタンス）
- **ユニーク制約**: (JOB_NAME, JOB_KEY) の組み合わせは一意

### 2. BATCH_JOB_EXECUTION - ジョブの実行履歴

**役割**: 「いつジョブが実行されたか」を記録

```
JOB_EXECUTION_ID | JOB_INSTANCE_ID | STATUS    | START_TIME | END_TIME
1                | 1               | COMPLETED | 17:10:00   | 17:10:01
2                | 1               | FAILED    | 17:15:00   | 17:15:02
```

- **STATUS**: STARTING, STARTED, COMPLETED, FAILED, STOPPED
- **重要**: 同じインスタンスでも複数回実行できる（リトライ時など）

### 3. BATCH_JOB_EXECUTION_PARAMS - ジョブパラメータ

**役割**: ジョブ実行時に渡されたパラメータを保存

```
JOB_EXECUTION_ID | PARAMETER_NAME | PARAMETER_VALUE
1                | timestamp      | 1730450000000
1                | inputFile      | sample.csv
```

- **IDENTIFYING**: 'Y'ならJOB_KEYの計算に使用される

### 4. BATCH_STEP_EXECUTION - ステップの実行履歴

**役割**: 各ステップの詳細な実行統計

```
STEP_EXECUTION_ID | JOB_EXECUTION_ID | STEP_NAME  | READ_COUNT | WRITE_COUNT | STATUS
1                 | 1                | sampleStep | 5          | 5           | COMPLETED
```

**重要な統計情報**:
- `READ_COUNT`: 読み込んだアイテム数
- `WRITE_COUNT`: 書き込んだアイテム数
- `FILTER_COUNT`: フィルターされた（スキップされた）アイテム数
- `COMMIT_COUNT`: コミット回数
- `ROLLBACK_COUNT`: ロールバック回数
- `READ_SKIP_COUNT`: Reader でスキップされた数
- `WRITE_SKIP_COUNT`: Writer でスキップされた数
- `PROCESS_SKIP_COUNT`: Processor でスキップされた数

### 5. BATCH_STEP_EXECUTION_CONTEXT - ステップの状態管理

**役割**: ステップの実行中の状態を保存（リスタート時に復元）

```
STEP_EXECUTION_ID | SHORT_CONTEXT                        | SERIALIZED_CONTEXT
1                 | {"batch.taskletType":"..."}          | {...}
```

- **例**: 「100万件中50万件まで処理済み」といった進捗状況
- **SHORT_CONTEXT**: 簡単な情報（2500文字まで）
- **SERIALIZED_CONTEXT**: 複雑なオブジェクトをシリアル化して保存

### 6. BATCH_JOB_EXECUTION_CONTEXT - ジョブの状態管理

**役割**: ジョブ全体の実行コンテキスト（複数ステップ間で共有）

```
JOB_EXECUTION_ID | SHORT_CONTEXT | SERIALIZED_CONTEXT
1                | {...}         | {...}
```

- **用途**: ステップ間でデータを受け渡す時に使用

## テーブル間の関係図

```
BATCH_JOB_INSTANCE (ジョブ定義)
    ↓ 1:N
BATCH_JOB_EXECUTION (実行履歴)
    ↓ 1:N                    ↓ 1:1
BATCH_STEP_EXECUTION    BATCH_JOB_EXECUTION_CONTEXT
    ↓ 1:1                    (ジョブコンテキスト)
BATCH_STEP_EXECUTION_CONTEXT
    (ステップコンテキスト)

BATCH_JOB_EXECUTION
    ↓ 1:N
BATCH_JOB_EXECUTION_PARAMS
    (パラメータ)
```

## 実際の動作フロー

`sampleJob`を実行した時の流れ：

```
1. JobLauncher.run() が呼ばれる
   ↓
2. BATCH_JOB_INSTANCE に「sampleJob」が登録（初回のみ）
   ↓
3. BATCH_JOB_EXECUTION に実行レコード作成（STATUS=STARTING）
   ↓
4. BATCH_JOB_EXECUTION_PARAMS にパラメータ保存
   ↓
5. sampleStepが開始
   ↓
6. BATCH_STEP_EXECUTION にステップレコード作成
   ↓
7. Reader → Processor → Writer を繰り返し
   - 10件ずつコミット（chunkサイズ）
   - READ_COUNT, WRITE_COUNT などがインクリメントされる
   ↓
8. ステップ完了（STATUS=COMPLETED）
   ↓
9. ジョブ完了（STATUS=COMPLETED）
```

## なぜこんなに複雑な管理が必要なのか？

Spring Batchが本格的なバッチ処理に必要な以下の機能を提供するためです：

1. **リスタート機能**: 途中で失敗しても、中断した場所から再開できる
2. **監視**: 「何件処理したか」「何件スキップしたか」を追跡
3. **べき等性**: 同じパラメータで実行済みなら再実行しない（設定により変更可能）
4. **統計レポート**: 処理件数、実行時間などの分析が可能
5. **並列処理の管理**: 複数のジョブやステップを並行実行する際の制御

## H2コンソールでの確認方法

### ジョブの実行履歴を確認

```sql
SELECT je.JOB_EXECUTION_ID, ji.JOB_NAME, je.STATUS,
       je.START_TIME, je.END_TIME
FROM BATCH_JOB_EXECUTION je
JOIN BATCH_JOB_INSTANCE ji ON je.JOB_INSTANCE_ID = ji.JOB_INSTANCE_ID
ORDER BY je.JOB_EXECUTION_ID DESC;
```

### ステップの詳細統計

```sql
SELECT STEP_EXECUTION_ID, STEP_NAME,
       READ_COUNT, FILTER_COUNT, WRITE_COUNT,
       COMMIT_COUNT, ROLLBACK_COUNT, STATUS
FROM BATCH_STEP_EXECUTION
ORDER BY STEP_EXECUTION_ID DESC;
```

### ジョブに渡されたパラメータ

```sql
SELECT PARAMETER_NAME, PARAMETER_VALUE, PARAMETER_TYPE
FROM BATCH_JOB_EXECUTION_PARAMS
WHERE JOB_EXECUTION_ID = 1;
```

### 全ての統計を一度に見る

```sql
SELECT
    ji.JOB_NAME,
    je.JOB_EXECUTION_ID,
    je.STATUS as JOB_STATUS,
    se.STEP_NAME,
    se.READ_COUNT,
    se.FILTER_COUNT,
    se.WRITE_COUNT,
    se.COMMIT_COUNT,
    se.STATUS as STEP_STATUS,
    je.START_TIME,
    je.END_TIME
FROM BATCH_JOB_INSTANCE ji
JOIN BATCH_JOB_EXECUTION je ON ji.JOB_INSTANCE_ID = je.JOB_INSTANCE_ID
LEFT JOIN BATCH_STEP_EXECUTION se ON je.JOB_EXECUTION_ID = se.JOB_EXECUTION_ID
ORDER BY je.JOB_EXECUTION_ID DESC, se.STEP_EXECUTION_ID;
```

## 実際のプロジェクトでの活用例

### 1. リスタート機能

100万件のデータを処理中、50万件目でエラーが発生した場合：

```java
// ExecutionContextに進捗を保存
executionContext.putLong("last_processed_id", 500000);

// 再実行時、このIDから処理を再開
Long lastProcessedId = executionContext.getLong("last_processed_id", 0L);
```

### 2. スキップ処理の追跡

不正なデータをスキップしながら処理を継続：

```java
@Bean
public Step step() {
    return stepBuilderFactory.get("step")
        .<Input, Output>chunk(10)
        .reader(reader())
        .processor(processor())
        .writer(writer())
        .faultTolerant()
        .skipLimit(100)  // 最大100件までスキップ許容
        .skip(ValidationException.class)
        .build();
}
```

スキップされた件数は`BATCH_STEP_EXECUTION`の`PROCESS_SKIP_COUNT`に記録されます。

### 3. ジョブの並列実行制御

同じジョブを異なるパラメータで並列実行：

```bash
# ジョブ1: 東日本エリアのデータを処理
curl -X POST http://localhost:8080/api/batch/jobs/importJob \
  -d '{"jobParameters": {"region": "east"}}'

# ジョブ2: 西日本エリアのデータを処理
curl -X POST http://localhost:8080/api/batch/jobs/importJob \
  -d '{"jobParameters": {"region": "west"}}'
```

それぞれ異なる`JOB_INSTANCE`として管理されます。

## まとめ

Spring Batchのメタデータ管理は：

- **信頼性**: 障害発生時の復旧を可能にする
- **可視性**: 処理状況をリアルタイムで把握できる
- **保守性**: 過去の実行履歴から問題を分析できる

このメタデータ管理があるおかげで、本番環境で大量データを処理する際に「どこまで処理したか」「何が失敗したか」を正確に追跡できます。
