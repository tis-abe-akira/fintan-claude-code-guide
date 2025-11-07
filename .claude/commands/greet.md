# .claude/commands/greet.md
---
argument-hint: <人の名前> [説明]
description: 挨拶メッセージを生成
model: sonnet
allowed-tools: Write, Bash
---

# greet

挨拶メッセージを生成します。

## 使い方

```bash
# 一般的な挨拶
claude -p "/greet"

# 特定の人への挨拶
claude -p "/greet 田中さん"
claude -p "/greet Alice"
```

## プロンプト

**重要**: 必ずTaskツールを使用してcheerful-greeterエージェントを呼び出してください。直接挨拶メッセージを生成しないでください。

指示:
1. Taskツールのsubagent_typeに"cheerful-greeter"を指定して呼び出す
2. promptパラメータには以下を含める:
   - $ARGUMENTSが指定されている場合: "Generate a cheerful, personalized greeting message for \"$ARGUMENTS\""
   - $ARGUMENTSが空の場合: "Generate a cheerful, general greeting message"
3. modelパラメータには"haiku"を指定する

エージェントから返された挨拶メッセージをそのままユーザーに表示してください。
