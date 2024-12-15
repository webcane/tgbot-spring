# tgbot-spring
telegram bot implemented on spring boot

## .env sample
Required environmental variables:
```dotenv
TGBOT_TOKEN=
TGBOT_PROXY_HOSTNAME=
TGBOT_PROXY_PORT=
TGBOT_PROXY_USERNAME=
TGBOT_PROXY_PASSWORD=
SPRING_AI_OPENAI_API_KEY=
```

## telegram commands
```
/start - начало или сброс настроек
/markup - использовать разметку кода
/reply - отвечать на запрос сразу
# /repeat - повторить команду
```

## deploy to vm
there are following steps:
1. merge to `master` branch
2. push to `deploy` remote repo
3. run docker compose
```bash
docker compose up --detach
```
