# fintan-claude-code-guide

本プロジェクトは、[Fintan Claude Code開発ガイド](https://fintan-contents.github.io/gai-dev-guide/claude-code-guide/introduction/)を用いた練習用プロジェクトです。

## 目的

Claude Codeを使用した開発手法を実践的に学習することを目的としています。本プロジェクトでは、以下のトピックを扱います：

- Spring Batchアプリケーションの開発
- REST APIの実装
- データベース連携
- バッチ処理の実装とテスト
- プロジェクト構造の理解

## プロジェクト構成

```
fintan-claude-code-guide/
├── product-import/     # 商品マスターインポートバッチアプリケーション
├── task-api/           # タスク管理API
├── tmp/                # 一時ファイル保存用
├── CLAUDE.md           # Claude Code用プロジェクトガイド
└── README.md           # 本ファイル
```

### product-import

Spring Batchを使用した商品マスターCSVインポートバッチアプリケーションです。

- 技術スタック: Spring Boot, Spring Batch, MyBatis, H2/PostgreSQL
- 主な機能: CSVインポート、バリデーション、UPSERT処理

詳細は[product-import/README.md](./product-import/README.md)を参照してください。

### task-api

タスク管理REST APIアプリケーションです。

詳細は[task-api/README.md](./task-api/README.md)を参照してください。

## Claude Code使用ガイド

本プロジェクトでClaude Codeを効果的に使用するためのガイドは[CLAUDE.md](./CLAUDE.md)を参照してください。

## 参考資料

- [Fintan Claude Code開発ガイド](https://fintan-contents.github.io/gai-dev-guide/claude-code-guide/introduction/)
- [Claude Code公式ドキュメント](https://docs.claude.com/en/docs/claude-code/)

## ライセンス

MIT License
