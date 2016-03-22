# :envelope: TempMail Java Library [![Build Status](https://travis-ci.org/flicus/TempMail.png)](https://travis-ci.org/flicus/TempMail) [![Release](https://jitpack.io/v/flicus/TempMail.svg)](https://jitpack.io/#flicus/TempMail)
This is a Java library, implementing [TempMail] (https://temp-mail.ru/) API for temporary e-mail boxes manipulating  

First of all, the service itself accept e-mails on any address of the domains belonging to it. You do not need to do anything specific to create your own mailbox. You just need to decide which domain name you will use, and which e-mail address do you like. Then start sending e-mails on that address and check it with the API. Keep in mind, service will store e-mails only for the short period of time (they mention 10 minutes)  

There are two implementations of that API available here: 

- [Standalone] (#standalone), for using with pure Java SE
- [Vert.x service] (#vertx-service), for using with [Vert.x] (http://vertx.io/) reactive toolkit 

## Standalone
Create client object with static method of the interface `TempMailClient client = TempMailClient.create();`. If you need to use Proxy, you may configure it: 
```Java
TempMailOptions options = new TempMailOptions().setProxy("proxyHost", 8080, "http");
TempMailClient client = TempMailClient.create(options);
```
Then call the methods of the class. Standalone version supporting synchronous and asynchronous calls.

### Getting the list of the supported domain names synchronously
```Java
JSONObject res = client.getSupportedDomains();
JSONArray domains = res.getJSONArray("result");
Iterator iterator = domains.iterator();
while (iterator.hasNext()) {
    System.out.println(iterator.next());    //string with domain name here
}
```

### Getting the list of the supported domain names asynchronously
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

### Getting the list of the e-mails in the box synchronously
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

### Getting the list of the e-mails in the box asynchronously
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

### Monitoring mailbox for the new e-mails. Only new e-mails will be reported
For this you need to register listener which will be called every time you receive new e-mail(s) in your mailbox
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

