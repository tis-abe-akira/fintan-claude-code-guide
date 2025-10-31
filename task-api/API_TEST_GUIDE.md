# Task API テストガイド

このドキュメントは、Task Management APIの全エンドポイントをcurlコマンドでテストするためのガイドです。

## 前提条件

アプリケーションが起動していることを確認してください。

```bash
mvn spring-boot:run
```

デフォルトでは `http://localhost:8080` で起動します。

---

## エンドポイント一覧

### 1. 全タスク取得

**エンドポイント:** `GET /api/tasks`
**説明:** すべてのタスクを取得します。

```bash
curl -X GET http://localhost:8080/api/tasks
```

**期待されるレスポンス:**
- ステータスコード: `200 OK`
- ボディ: タスクの配列（JSON）

---

### 2. タスク作成

**エンドポイント:** `POST /api/tasks`
**説明:** 新しいタスクを作成します。

#### 基本的なタスク作成

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "タスク1",
    "description": "最初のタスクです",
    "completed": false
  }'
```

#### completedを省略（デフォルトでfalse）

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "タスク2",
    "description": "2番目のタスクです"
  }'
```

#### 説明なしのタスク

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "シンプルなタスク",
    "completed": false
  }'
```

**期待されるレスポンス:**
- ステータスコード: `201 Created`
- ボディ: 作成されたタスク（id、createdAt、updatedAtが自動設定される）

---

### 3. 特定タスク取得

**エンドポイント:** `GET /api/tasks/{id}`
**説明:** 指定されたIDのタスクを取得します。

```bash
# タスクID 1を取得
curl -X GET http://localhost:8080/api/tasks/1
```

**期待されるレスポンス:**
- ステータスコード: `200 OK`
- ボディ: 指定されたタスク（JSON）

#### 存在しないタスクを取得（エラーケース）

```bash
curl -X GET http://localhost:8080/api/tasks/999
```

**期待されるレスポンス:**
- ステータスコード: `404 Not Found`
- ボディ: エラーメッセージ

---

### 4. タスク更新

**エンドポイント:** `PUT /api/tasks/{id}`
**説明:** 指定されたIDのタスクを更新します。

```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "更新されたタスク",
    "description": "このタスクは更新されました",
    "completed": true
  }'
```

**期待されるレスポンス:**
- ステータスコード: `200 OK`
- ボディ: 更新されたタスク（updatedAtが更新される）

#### 存在しないタスクを更新（エラーケース）

```bash
curl -X PUT http://localhost:8080/api/tasks/999 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "更新失敗",
    "description": "このタスクは存在しません",
    "completed": false
  }'
```

**期待されるレスポンス:**
- ステータスコード: `404 Not Found`
- ボディ: エラーメッセージ

---

### 5. タスク削除

**エンドポイント:** `DELETE /api/tasks/{id}`
**説明:** 指定されたIDのタスクを削除します。

```bash
curl -X DELETE http://localhost:8080/api/tasks/1
```

**期待されるレスポンス:**
- ステータスコード: `204 No Content`
- ボディ: なし

#### 存在しないタスクを削除（エラーケース）

```bash
curl -X DELETE http://localhost:8080/api/tasks/999
```

**期待されるレスポンス:**
- ステータスコード: `404 Not Found`
- ボディ: エラーメッセージ

---

### 6. 完了状態切り替え

**エンドポイント:** `PATCH /api/tasks/{id}/toggle`
**説明:** 指定されたIDのタスクの完了状態を反転します（true ⇔ false）。

```bash
curl -X PATCH http://localhost:8080/api/tasks/1/toggle
```

**期待されるレスポンス:**
- ステータスコード: `200 OK`
- ボディ: completedが反転したタスク

#### 存在しないタスクの状態切り替え（エラーケース）

```bash
curl -X PATCH http://localhost:8080/api/tasks/999/toggle
```

**期待されるレスポンス:**
- ステータスコード: `404 Not Found`
- ボディ: エラーメッセージ

---

## バリデーションエラーのテスト

### タイトルが空（エラーケース）

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "",
    "description": "タイトルが空です",
    "completed": false
  }'
```

**期待されるレスポンス:**
- ステータスコード: `400 Bad Request`
- ボディ: バリデーションエラーメッセージ

### タイトルが100文字超過（エラーケース）

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
    "description": "タイトルが101文字です",
    "completed": false
  }'
```

**期待されるレスポンス:**
- ステータスコード: `400 Bad Request`
- ボディ: バリデーションエラーメッセージ

### 説明が500文字超過（エラーケース）

```bash
# 501文字の説明を含むリクエスト
LONG_DESC=$(printf 'a%.0s' {1..501})
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"長い説明のタスク\",
    \"description\": \"$LONG_DESC\",
    \"completed\": false
  }"
```

**期待されるレスポンス:**
- ステータスコード: `400 Bad Request`
- ボディ: バリデーションエラーメッセージ

---

## テストシナリオ例

以下は、APIの動作を確認するための完全なテストシナリオです。

```bash
# 1. 初期状態を確認（空のリスト）
echo "=== 1. 全タスク取得（初期状態） ==="
curl -X GET http://localhost:8080/api/tasks
echo -e "\n"

# 2. タスクを3つ作成
echo "=== 2. タスク作成（3件） ==="
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"買い物","description":"牛乳とパンを買う","completed":false}'
echo -e "\n"

curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"掃除","description":"リビングを掃除する","completed":false}'
echo -e "\n"

curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"読書","description":"技術書を読む","completed":true}'
echo -e "\n"

# 3. 全タスク取得（3件あることを確認）
echo "=== 3. 全タスク取得（3件） ==="
curl -X GET http://localhost:8080/api/tasks
echo -e "\n"

# 4. 特定タスク取得
echo "=== 4. タスクID 1を取得 ==="
curl -X GET http://localhost:8080/api/tasks/1
echo -e "\n"

# 5. タスク更新
echo "=== 5. タスクID 1を更新 ==="
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"買い物（完了）","description":"牛乳とパンを買った","completed":true}'
echo -e "\n"

# 6. 完了状態切り替え
echo "=== 6. タスクID 2の完了状態を切り替え ==="
curl -X PATCH http://localhost:8080/api/tasks/2/toggle
echo -e "\n"

# 7. タスク削除
echo "=== 7. タスクID 3を削除 ==="
curl -X DELETE http://localhost:8080/api/tasks/3
echo -e "\n"

# 8. 最終状態を確認
echo "=== 8. 全タスク取得（最終状態） ==="
curl -X GET http://localhost:8080/api/tasks
echo -e "\n"

# 9. 存在しないタスクを取得（404エラー）
echo "=== 9. 存在しないタスクを取得（404エラー） ==="
curl -X GET http://localhost:8080/api/tasks/999
echo -e "\n"

# 10. バリデーションエラー（タイトルが空）
echo "=== 10. バリデーションエラー（タイトルが空） ==="
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"","description":"エラーテスト","completed":false}'
echo -e "\n"
```

---

## レスポンス形式

### 成功レスポンス（タスク）

```json
{
  "id": 1,
  "title": "タスク1",
  "description": "最初のタスクです",
  "completed": false,
  "createdAt": "2025-10-31T22:00:00",
  "updatedAt": "2025-10-31T22:00:00"
}
```

### エラーレスポンス（404 Not Found）

```json
{
  "timestamp": "2025-10-31T22:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Task not found with id: 999",
  "path": "/api/tasks/999"
}
```

### エラーレスポンス（400 Bad Request - バリデーションエラー）

```json
{
  "timestamp": "2025-10-31T22:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/tasks",
  "details": [
    "title: Title must not be blank"
  ]
}
```

---

## Tips

### レスポンスを見やすくする

`jq`コマンドを使用すると、JSONレスポンスが整形されて見やすくなります。

```bash
curl -X GET http://localhost:8080/api/tasks | jq
```

### HTTPステータスコードを確認

`-i` オプションでヘッダーも含めて表示できます。

```bash
curl -i -X GET http://localhost:8080/api/tasks
```

### verbose モード

`-v` オプションで詳細なリクエスト/レスポンス情報を表示できます。

```bash
curl -v -X GET http://localhost:8080/api/tasks
```
