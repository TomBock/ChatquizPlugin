## Simple Chatquiz
A lightweight and easy-to-use chat quiz plugin for Minecraft (Paper).  
Ask questions in chat, reward correct answers, and add simple fun to your server.

![ChatQuiz Screenshot](assets/example.jpg)

## Features:
- Global chat quiz with custom questions
- Answers can be typed in chat
- Customizable rewards (items / commands)
- Customizable messages
- No dependencies

## Configs
After first startup, the plugin creates:
- _questions.yml_ - Questions and answers
- _rewards.yml_ - Rewards can be items or commands
- _settings.yml_ - Time limits
- _msg.yml_ - Chat messages

## Commands:
| Command                                 | Description                                                               |
|-----------------------------------------|---------------------------------------------------------------------------|
| `/chatquiz start <amount of questions>` | Starts a new chat quiz with the given amount of questions chosen randomly |
| `/chatquiz stop`                        | Stops the current chat quiz                                               | 
| `/chatquiz reload`                      | Reloads the plugin config files                                           |

## Permissions:
| Permission Node  | Description                | Default |
|------------------|----------------------------|---------|
| `chatquiz.admin` | Use chat quiz commands     | op      |

