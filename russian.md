# :envelope: TempMail Java Library [![Release](https://jitpack.io/v/flicus/TempMail.svg)](https://jitpack.io/#flicus/TempMail)
Java библиотека, реализующая [TempMail] (https://temp-mail.ru/) API для автоматизации работы с временными почтовыми ящиками  

Для начала - сам сервис принимает почту на любой адрес в своих доменах. Нет необходимости регистрировать почтовый ящик, нужно просто выбрать подходящий домен и адрес ящика в домене. После этого можно слать на этот адрес почту и проверять ее при помощи API. Сервис хранит входящую почту в течении короткого времени (они у себя пишут 10 минут)

Доступны два варианта реализации этого API:

- [Standalone] (#standalone), для использования с чистым Java SE
- [Vert.x service] (#vertx-service), для использования с реактивной библиотекой [Vert.x] (http://vertx.io/) 

## Standalone
Создать клиента `TempMailClient client = TempMailClient.create();`. Если необходимо использование прокси-сервера: 
```Java
TempMailOptions options = new TempMailOptions().setProxy("proxyHost", 8080, "http");
TempMailClient client = TempMailClient.create(options);
```
После этого можно вызывать методы класса. Поддерживаются синхронные и асинхронные методы.

### Получить список поддерживаемых доменов, синхронно
```Java
JSONObject res = client.getSupportedDomains();
JSONArray domains = res.getJSONArray("result");
Iterator iterator = domains.iterator();
while (iterator.hasNext()) {
    System.out.println(iterator.next());    //string with domain name here
}
```

### Получить список поддерживаемых доменов, асинхронно
```Java
client.getSupportedDomains(res -> {
    if (res.success()) {
        JSONArray domains = res.result().getJSONArray("result");
        Iterator iterator = domains.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());    //string with domain name here
        }    
    }
});
```

### Получить список емайлов в ящике, синхронно
```Java
JSONObject res = client.getMessages("my@email.org");
if (res != null && res.length() > 0) {
    Iterator iterator = res.getJSONArray("result").iterator();
    while (iterator.hasNext()) {
        JSONObject email = (JSONObject)iterator.next();
        System.out.println(
            String.format("E-mail from: %s, subject: %s",
                email.getString(Constants.MAIL_FROM),
                email.getString(Constants.MAIL_SUBJECT)));
    }    
}
```

### Получить список емайлов в ящике, асинхронно
```Java
client.getMessages("my@email.org", res -> {
    if (res != null && res.success()) {
        Iterator iterator = res.result().getJSONArray("result").iterator();
        while (iterator.hasNext()) {
            //blablabla
        }
    }
});
```

### Отслеживание новых емайлов в ящике. Возвращаются только новые емайлы
Для этого необходимо зарегистрировать обработчик, который будет вызываться каждый раз, когда получен новый емайл (или емайлы) 
```Java
client.addMailListener("pipka@shotmail.ru", event -> {
    if (event.success()) {
        JSONArray newEmails = event.result().getJSONArray("result");
        if (newEmails != null && newEmails.length() > 0) {
            //handle new emails
        }
    }
});
```

## Vert.x service

