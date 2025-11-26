## Chatquiz Plugin
A lightweight chat-based quiz system for Minecraft (Paper).
Players answer questions in chat, earn rewards, and compete during with each other.

_This version is customized for the Phoenix server.
A generic public version can easily be created later._

## Features:
- Chat based quiz rounds with any number of questions
- Fully configurable questions and rewards

## Commands:
| Command                    | Description                                                               |
|----------------------------|---------------------------------------------------------------------------|
| `/chatquiz start <amount>` | Starts a new chat quiz with the given amount of questions chosen randomly |
| `/chatquiz stop`           | Stops the current chat quiz                                               | 

## Permissions:
| Permission Node  | Description                | Default |
|------------------|----------------------------|---------|
| `chatquiz.start` | Use chat quiz commands     | op      |

## Configs:
- _config.yml_ - Main config for rewards, settings and messages
- _questions.yml_ - Questions and answers
