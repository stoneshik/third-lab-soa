# Сервис-ориентированная архитектура

## Лабораторная работа № 3

### Задание

Вариант 4352

Переработать веб-сервисы из лабораторной работы #2 таким образом, чтобы они реализовывали основные концепции микросервисной архитектуры. Для этого внести в оба сервиса -- "вызываемый" и "вызывающий" перечисленные ниже изменения.

Изменения в "вызываемом" сервисе:
- Сконфигурировать окружение для работы сервиса на платформе Spring Boot.
- Запустить второй экземпляр сервиса на другом порту. Реализовать балансировку нагрузки между экземплярами с помощью Haproxy.
- Реализовать механизм Service Discovery. Для этого установить Consul и интегрировать свой сервис с ним, автоматически регистрируя в момент запуска.

Изменения в "вызывающем" сервисе:
- Разделить приложение на два модуля -- веб-приложение с веб-сервисом и EJB-jar с бизнес-компонентами.
- Переместить всю логику из класса сервиса в Stateless EJB. В классе сервиса оставить только обращение к методам бизнес-интерфейса. EJB-компонент должен быть доступен удалённо (иметь Remote-интерфейс).
- Сформировать на уровне сервера приложений пул компонентов EJB настраиваемой мощности, динамически расширяемый при увеличении нагрузки.
- Настроить второй экземпляр сервера приложений на другом порту, "поднять" на нём вторую копию веб-сервиса и пула EJB.
- Настроить балансировку нагрузки на оба запущенных узла через Haproxy.
Оба веб-сервиса и клиентское приложение должны сохранить полную совместимость с API, реализованными в рамках предыдущих лабораторных работ.

### Комментарии к реализации

Для запуска тестов нужно задать значение переменной окружения `SPRING_PROFILES_ACTIVE` равное `test` - в этом профиле используется БД поднимаемая `testcontainers`

Для запуска в обычном режиме значение переменной окружения `SPRING_PROFILES_ACTIVE` равное `dev`

Файл `.env` должен находится в директории `resources`

Пример `.env` файла находится в `env.example`

Подключение к серверу:
```
ssh s33xxxx@se.ifmo.ru -p 2222
```

Проброс порта для helios:
```
ssh -L 8080:localhost:33xxxx s33xxxx@se.ifmo.ru -p 2222
```

Url подключения к БД
```
jdbc:postgresql://localhost:5432/studs
```

Генерация ключа:
```
keytool -genkeypair -alias spring -keyalg RSA -keysize 4096 \
  -validity 3650 -keystore spring.p12 \
  -storetype PKCS12 -storepass changeit -keypass changeit \
  -dname "CN=localhost, OU=Development, O=Company, L=City, ST=State, C=RU"
```

Экспорт сертификата:
```
keytool -exportcert -alias spring -keystore spring.p12 -storetype PKCS12 -storepass changeit -file spring.crt
```

Добавление сертификата в trustore:
```
keytool -importcert -alias spring -file spring.crt -keystore spring-truststore.p12 -storetype PKCS12 -storepass changeit -noprompt
```

Экспортируем сертификат и ключ для HAProxy:
```
# 1. Экспорт приватного ключа
openssl pkcs12 -in spring.p12 -nocerts -nodes -out spring.key -passin pass:changeit
# 2. Экспорт сертификата
openssl pkcs12 -in spring.p12 -clcerts -nokeys -out spring.crt -passin pass:changeit
# 3. Объединяем в один PEM для HAProxy
cat spring.key spring.crt > spring.pem
```

Установка HAProxy:
```
make TARGET=linux-glibc USE_OPENSSL=1 USE_ZLIB=1 USE_PCRE=1
make install PREFIX=$HOME/haproxy
mkdir ~/haproxy/etc
```

Запуск:
```
~/haproxy/sbin/haproxy -f ~/haproxy/etc/haproxy.cfg
```

Копирование конфига:
```
cp haproxy.cfg ~/haproxy/etc/haproxy.cfg
```

Сбор jar файла с пропуском тестов
```
mvn package -DskipTests
```

Открыть статистику haproxy:
```
http://localhost:33401/stats
```

Запуск сервисов:
```
SOA_SERVICE_PORT=33511 java -jar soa-0.0.1-SNAPSHOT.jar
```
```
SOA_SERVICE_PORT=33521 java -jar soa-0.0.1-SNAPSHOT.jar
```

Устанавливаем Consul:
```
sudo apt install consul
```

Запуск consul с заданием значений всех портов:
```
consul agent -dev -ui \
  -client=0.0.0.0 \
  -bind=127.0.0.1 \
  -http-port=33410 \
  -server-port=33411 \
  -serf-lan-port=33412 \
  -serf-wan-port=33413 \
  -dns-port=33414
```

Остановка consul:
```
consul leave -http-addr=127.0.0.1:33410
```

Открыть consul:
```
http://localhost:33410
```

**Порты которые нужно пробросить:**
- 33510 - обращение к сервисам на spring
- 33610 - обращение к сервисам на wildfly
- 33401 - статистика haproxy
- 33410 - consul

### Ссылки на репозитории лабораторной

1. Ссылка на основной вызываемый сервис реализованный на Spring Boot - https://github.com/stoneshik/third-lab-soa
2. Ссылка на второй вызывающий сервис реализованный на JAX-RS - https://github.com/stoneshik/third-lab-soa-second
3. Ссылка на фронтенд - https://github.com/stoneshik/third-lab-soa-frontend
