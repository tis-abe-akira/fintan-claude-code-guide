# .claude/commands/create-entity.md
---
argument-hint: <クラス名> <テーブル名> <フィールド定義>
description: JPA エンティティクラスを作成
allowed-tools: Edit, Write, Read
---

以下の仕様でJPAエンティティクラスを作成してください：

## 基本要件
- クラス名: $1
- テーブル名: $2
- フィールド定義: $3

## 技術要件
- JPA アノテーションを使用
- BaseEntity を継承
- バリデーションアノテーションを適用
- toString, equals, hashCode メソッドを実装

## コーディング規約
- Google Java Style Guide準拠
- JavaDoc コメントを含む
- フィールドはprivateで定義
- 適切なコンストラクタを提供

実装時は、フィールド定義を解析して適切な型とアノテーションを適用してください。
